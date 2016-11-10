# Java Evolutionary Biology Library
A Java library for evolutionary biology and bioinformatics, including objects representing biomolecular sequences, 
multiple sequence alignments and phylogenetic trees.

## Requirements
* Java Development Kit 1.6+
* Apache Ant

## Installation
Run the following command from within the jebl directory:

    ant dist
    
The built *jebl.jar* library in the dist folder can then be included in your project.

### API Documentation
To produce the Javadoc in the *doc* directory run the following:
    
    ant document
    
## Development
This project includes three components:
1. jebl
2. org.virion.jam
3. org.virion.jam.maconly

*org.virion.jam.maconly* that can only be built under Mac OS X.  A pre-built version is located in the lib directory 
so it can be included on the classpath for non-Macs.  If anything is changed in this package then it has to be built separately 
  (see the -mac-only targets).
  
### Operating System
If you are using an IDE and not on a Mac then you will need to exclude org.virion.jam.maconly in order to compile the
project.