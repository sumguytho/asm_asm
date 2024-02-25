package org.objectweb.asm;

import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Removes cyclic references from signatures.
 * The idea is that we hide the real visitor on each call to {@link visitSuperclass} and {@link visitInterface},
 * the first {@link visitClassType} then exposes the top level superclass name and, if it's not cyclic, we set
 * the visitor back to what it should be and reinstate the calls that were omitted. Otherwise we leave the
 * visitor as is, it will ignore all the subsequent parsing until the next call to {@link visitSuperclass} or
 * {@link visitInterface}.
 */
public class FixCyclicSignatureVisitor extends SignatureVisitor {
	private String superclassName;
	private SignatureVisitor sv;
	private SignatureVisitor realsv;
	private SignatureVisitor sinksv = new SignatureSink();
	
	// Indicates whether a call to visitClassType denotes the
	// class itself.
	private boolean visitingSuperclass;
	private boolean visitingSuperinterface;
	
	public FixCyclicSignatureVisitor(final String superclassName, final SignatureVisitor sv) {
		super(Opcodes.ASM9);
		this.superclassName = superclassName;
		this.realsv = sv;
		this.sv = sv;
	}

	@Override
	public void visitClassType(final String name) {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitClassType name=%s", name));
		boolean visitingTopLevelIdentifier = visitingSuperclass || visitingSuperinterface;
		if (visitingTopLevelIdentifier) {
			if (superclassName.equals(name)) {
				// the sv is already set to sink at this point, no extra work to do
				// unless we are dealing with a superclass
				if (visitingSuperclass) {
					realsv.visitSuperclass();
					// superclass should always be present, unlike an interface we can't just skip it
					realsv.visitClassType("java/lang/Object");
					realsv.visitEnd();
				}
			}
			else {
				sv = realsv;
				// reinstate the calls we omitted previously
				if (visitingSuperinterface) {
					sv.visitInterface();
				}
				else if (visitingSuperclass) {
					sv.visitSuperclass();
				}
			}
		}
		visitingSuperclass = false;
		visitingSuperinterface = false;

		sv.visitClassType(name);
	}

	@Override
	public void visitInnerClassType(final String name) {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitInnerClassType name=%s", name));
		sv.visitInnerClassType(name);
	}

	@Override
	public void visitFormalTypeParameter(final String name) {
		sv.visitFormalTypeParameter(name);
	}

	@Override
	public void visitTypeVariable(final String name) {
		sv.visitTypeVariable(name);
	}

	@Override
	public SignatureVisitor visitArrayType() {
		sv.visitArrayType();
		return this;
	}

	@Override
	public void visitBaseType(final char descriptor) {
		sv.visitBaseType(descriptor);
	}

	@Override
	public SignatureVisitor visitClassBound() {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitClassBound"));
		sv.visitClassBound();
		return this;
	}


	@Override
	public SignatureVisitor visitInterface() {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitInterface"));
		visitingSuperinterface = true;
		sv = sinksv;
		return this;
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitInterfaceBound"));
		sv.visitInterfaceBound();
		return this;
	}

	@Override
	public SignatureVisitor visitParameterType() {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitParameterType"));
		sv.visitParameterType();
		return this;
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitSuperclass"));
		visitingSuperclass = true;
		// don't delegate parser call anywhere until we are sure we need to
		sv = sinksv;
		return this;
	}

	@Override
	public void visitTypeArgument() {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitTypeArgument"));
		sv.visitTypeArgument();
	}

	@Override
	public SignatureVisitor visitTypeArgument(final char wildcard) {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitTypeArgument, wildcard=%c", wildcard));
		sv.visitTypeArgument(wildcard);
		return this;
	}

	@Override
	public void visitEnd() {
		System.out.println(String.format("FixCyclicSignatureVisitor.visitEnd"));
		sv.visitEnd();
	}
}
