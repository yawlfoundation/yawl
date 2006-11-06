/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.util.syntax;

/*
 * CCTokenMarker.java - C++ token marker
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */


/**
 * C++ token marker.
 *
 * @author Slava Pestov
 * @version $Id: SASTokenMarker.java,v 1.3 2004/07/20 19:55:11 maod Exp $
 */
public class SASTokenMarker extends CTokenMarker
{
	public SASTokenMarker()
	{
		super(true,getKeywords());
	}

	public static KeywordMap getKeywords()
	{
		if(keywords == null)
		{
			keywords = new KeywordMap(false);

			keywords.add("data", Token.KEYWORD1);
			keywords.add("proc", Token.KEYWORD1);
			keywords.add("run", Token.KEYWORD1);
			keywords.add("set", Token.KEYWORD1);
			keywords.add("if", Token.KEYWORD1);
			keywords.add("then", Token.KEYWORD1);
						
			
			keywords.add("merge", Token.KEYWORD3);
			keywords.add("length", Token.KEYWORD3);
			keywords.add("options", Token.KEYWORD3);
			keywords.add("label", Token.KEYWORD3);
			keywords.add("input", Token.KEYWORD3);
			keywords.add("cards", Token.KEYWORD3);
			keywords.add("output", Token.KEYWORD3);
			keywords.add("var", Token.KEYWORD3);
			keywords.add("title", Token.KEYWORD3);


			
			
			
			
      

			// non ANSI keywords
			keywords.add("NULL", Token.LITERAL2);
		}
		return keywords;
	}

	// private members
	private static KeywordMap keywords;
}
