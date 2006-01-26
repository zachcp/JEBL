package org.virion.jam.panels;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * @author rambaut
 *         Date: Jul 27, 2004
 *         Time: 9:32:24 AM
 */
public interface StatusProvider {

    /**
     * Status providers must be able to store a list of StatusListeners. They should
     * then call the appropriate methods on all of these to update the status.
     *
     * @param statusListener the StatusListener to be added
     */
    void addStatusListener(StatusListener statusListener);

    /**
     * Remove the given StatusListener from the provider's list.
     *
     * @param statusListener the StatusListener to be removed
     */
    void removeStatusListener(StatusListener statusListener);

    void fireStatusChanged(int status, String statusText);

    void statusButtonPressed();

    String getStatusText();
    int getStatus();

    void fireStatusButtonPressed();

    public void addOverrideProvider(StatusProvider provider);
    public void removeOverrideProvider(StatusProvider provider);


    public class Helper implements StatusProvider {
        private int lastStatus = StatusPanel.NORMAL;
        private String lastStatusText = "";
        private ArrayList listeners = new ArrayList();

        public synchronized void addStatusListener(StatusListener statusListener) {
            statusListener.statusChanged(lastStatus, lastStatusText);
            listeners.add(statusListener);
        }

        public synchronized void removeStatusListener(StatusListener statusListener) {
            statusListener.statusChanged(StatusPanel.NORMAL, "");
            listeners.remove(statusListener);
        }

        private synchronized void realFireStatusChanged() {
            int status = lastStatus;
            String statusText = lastStatusText;
            int index = overrideProviders.size()- 1;
            if (index >= 0) {
                StatusProvider override =(StatusProvider) overrideProviders.get(index);
                status = override.getStatus();
                statusText = override.getStatusText();
            }
            Iterator i = listeners.iterator();
            while (i.hasNext()) {
                ((StatusListener) i.next()).statusChanged(status, statusText);
            }

        }

        public synchronized void fireStatusChanged(int status, String statusText) {
            lastStatus = status;
            lastStatusText = statusText;
            realFireStatusChanged();
        }

        private ArrayList overrideListeners = new ArrayList ();
        private ArrayList overrideProviders = new ArrayList ();

        public synchronized void addOverrideProvider(final StatusProvider provider) {
            StatusListener listener = new StatusListener(){

                public void statusChanged(int status, String statusText) {
                    synchronized(StatusProvider.Helper.this) {
                        if (overrideProviders.size() > 0 && overrideProviders.get(overrideProviders.size()- 1)== provider) {
                            realFireStatusChanged();
                        }
                    }
                }
            };
            provider.addStatusListener(listener);
            overrideProviders.add(provider);
            overrideListeners.add(listener);
        }

        public synchronized void removeOverrideProvider(StatusProvider provider) {
            for (int i = 0; i < overrideProviders.size(); i++) {
                if(overrideProviders.get(i) == provider) {
                    overrideProviders.remove(i);
                    StatusListener listener =(StatusListener) overrideListeners.get(i);
                    provider.removeStatusListener(listener);
                    overrideListeners.remove(i);
                    break;
                }
            }
            realFireStatusChanged();
        }
        public synchronized void fireStatusButtonPressed(){
            int index = overrideProviders.size ();
            if (index > 0) {
                ((StatusProvider)overrideProviders.get(index - 1)).fireStatusButtonPressed();
            }
            else {
                statusButtonPressed();
            }
        }

        public void statusButtonPressed() {
            // do nothing... override
        }

        public synchronized int getStatus() {
            return lastStatus;
        }

        public synchronized String getStatusText() {
            return lastStatusText;
        }

    }


}