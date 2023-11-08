package com.github.maxopoly.kira.command.model.top;

import org.apache.logging.log4j.Logger;

import net.civmc.kira.Kira;

public abstract class TextInput <A, S extends InputSupplier> {

	private String identifier;
	private String[] alternativeIdentifiers;
	protected Logger logger;

	public TextInput(String identifier, String... alt) {
		this.identifier = identifier;
		this.alternativeIdentifiers = alt;
		this.logger = Kira.Companion.getInstance().getLogger();
	}

	/**
	 * @return Alternative commands, which will also execute this
	 */
	public String[] getAlternativeIdentifiers() {
		return alternativeIdentifiers;
	}

	/**
	 * @return The actual string entered to run this command
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	public abstract void handle(A argument, S supplier);

}
