package org.objectweb.asm;

public class StackFrameLookupResult {
	/* Other fields only make sense when this is set to true. */
	boolean isValid;
	int nextFrameOffset;
	int frameType;
	int offsetDelta;
	int localCount;
	int localCountDelta;
	int stackCount;
}
