/*
 * Unitex
 *
 * Copyright (C) 2001-2018 Université Paris-Est Marne-la-Vallée <unitex@univ-mlv.fr>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 */
package fr.umlv.unitex.process.commands;

import java.io.File;

/**
 * @author Sébastien Paumier
 * 
 */
public class ConcorDiffCommand extends CommandBuilder {
	public ConcorDiffCommand() {
		super("ConcorDiff");
	}

	public ConcorDiffCommand firstIndFile(File s) {
		protectElement(s.getAbsolutePath());
		return this;
	}

	public ConcorDiffCommand secondIndFile(File s) {
		protectElement(s.getAbsolutePath());
		return this;
	}

	public ConcorDiffCommand output(File s) {
		protectElement("-o" + s.getAbsolutePath());
		return this;
	}

	public ConcorDiffCommand font(String s) {
		protectElement("-f" + s);
		return this;
	}

	public ConcorDiffCommand fontSize(int size) {
		element("-s" + size);
		return this;
	}

	public ConcorDiffCommand diffOnly() {
		element("-d");
		return this;
	}
}
