package org.virion.jam.framework;

import java.awt.peer.ComponentPeer;
import java.lang.reflect.Field;

/**
 * Workaround for https://bugs.openjdk.java.net/browse/JDK-7124548.  See {@link #applyFix(ComponentPeer)}
 *
 * @author Matthew Cheung
 *         Created on 27/08/15 5:00 PM
 */
class MacOsJava8WindowMemoryLeakFix {

    private static final boolean IS_JAVA_7_OR_LATER = !System.getProperty("java.version").matches("1\\.[56]\\..*");
    private static final boolean IS_MAC = System.getProperty("os.name").startsWith("Mac OS");

    /**
     * Workaround for https://bugs.openjdk.java.net/browse/JDK-7124548.
     * <br><br>
     * The problem is that the JRE maintains a JNI reference to any Window created that prevents it from being garbage
     * collected.
     * <br><br>
     * The workaround is to set all fields of CPlatformWindow to null so that other objects it references can be
     * garbage collected even if it never will be.
     * <br><br>
     * <strong>Note</strong>: The window MUST have been disposed before calling this method.
     *
     * @param peer The {@link ComponentPeer} of the Window.  Obtained from calling {@link #getPeer()}
     */
    static void applyFix(ComponentPeer peer) {
        if(!(IS_MAC && IS_JAVA_7_OR_LATER) || peer == null) {
            return;
        }

        try {
            if (!clearFieldsFromObjectIfClass("sun.lwawt.LWComponentPeer", peer, "target")) {
                return;
            }

            Field windowField = peer.getClass().getDeclaredField("platformWindow");
            windowField.setAccessible(true);
            Object platformWindow = windowField.get(peer);
            if(platformWindow == null) {
                return;  // Nothing we can do
            }

            // There is a JNI reference to sun.lwawt.macosx.CPlatformWindow.  It'll remain in memory despite being
            // disposed.  We can't do anything about that.  But we can make sure that doesn't prevent other objects from
            // being garbage collected.
            clearFieldsFromObjectIfClass("sun.lwawt.macosx.CPlatformWindow", platformWindow, "target", "peer", "contentView", "responder");
        } catch (Exception e) {
            e.printStackTrace();
            // Fail in development.  Ignore in distributions.
            assert false: "We failed to workaround the memory leak: " + e.getMessage();
        }
    }

    private static boolean clearFieldsFromObjectIfClass(String expectedClassName, Object object, String... fieldNames) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> objectClass = object.getClass();
        Class<?> expectedClass = assertObjectIsType(objectClass, expectedClassName);
        if(expectedClass == null) {
            return false;
        }
        clearFields(object, expectedClass, fieldNames);
        return true;
    }

    private static Class<?> assertObjectIsType(Class actualClass, String expectedClassName) throws ClassNotFoundException {
        Class<?> expectedClass = Class.forName(expectedClassName);
        boolean isClass = expectedClass.isAssignableFrom(actualClass);
        assert isClass: "Expected " + expectedClassName + " but was " + actualClass;
        return isClass ? expectedClass : null;
    }

    private static void clearFields(Object object, Class<?> clazz, String... fieldNames) throws NoSuchFieldException, IllegalAccessException {
        for(String fieldName : fieldNames) {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, null);
        }
    }
}
