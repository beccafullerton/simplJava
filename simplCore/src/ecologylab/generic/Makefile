# current working directory
DIR	= cm/generic

JAVA_SRC = ParsedURL.java Generic.java AllocationDebugger.java Attribute.java Debug.java \
	   ThreadMaster.java Files.java Colors.java ObservableDebug.java \
	   StringTools.java DispatchTarget.java Monitor.java ImageTools.java \
	   Downloadable.java DownloadClosure.java DownloadMonitor.java \
	   Environment.java ApplicationEnvironment.java \
	   MathTools.java Palette.java DLL.java IntSlot.java Bounds.java \
	   ParsedURLType.java FloatSetElement.java FloatWeightSet.java \
	   Memory.java StringInputStream.java  Convenience.java\
	   StringInputStream.java Displacement1D.java Line2D.java \
	   StringBuffersPool.java SyncLinkedList.java \
	   ThreadDebugger.java TextDownloadMonitor.java SwingTools.java

# IntSlot used by Parser, Map

# Noise.java  Ordered.java Debug.java \
#	    List.java MimeList.java TestElement.java \

JAVA_ROOT=../../

MAKE_DIR = ../../../../makefiles
include $(MAKE_DIR)/applets.make

cabsign:	all

nsjar:		all

sunjar:		all

f:	Files.class
	java -classpath "$(CLASSPATH)" cm.generic.Files http://csdl.tamu.edu

g:	Generic.class
	java -classpath "$(CLASSPATH)" cm.generic.Generic

st:	StringTools.class
	java -classpath "$(CLASSPATH)" cm.generic.StringTools thisIsTheTime foo fooBar andAway FFFF

#	java -classpath "$(CLASSPATH)" cm.generic.StringTools http://www.nytimes.com http://csdl.tamu.edu/~andruid/courses http://csdl.tamu.edu/~andruid/courses/informedia/content.html http://csdl.tamu.edu/~andruid/courses/informedia/content.html?foo=bar http://csdl.tamu.edu/~andruid/courses/informedia/content.html#too

