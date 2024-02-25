package org.objectweb.asm;

import org.objectweb.asm.signature.SignatureVisitor;

public class SignatureSink extends SignatureVisitor {

	public SignatureSink() {
		super(Opcodes.ASM9);
	}

}
