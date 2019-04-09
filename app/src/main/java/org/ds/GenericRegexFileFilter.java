package org.ds;

import java.io.File;
import java.util.regex.Pattern;

public class GenericRegexFileFilter implements java.io.FileFilter
{
	private Pattern acceptableExtensions = null;
	private String descriptor = null;
	private boolean bAcceptDir = true;

	public GenericRegexFileFilter()
	{
       this(null, "", true);
	}

	public GenericRegexFileFilter(Pattern exts, String des)
	{
       this(exts, des, true);
	}

	public GenericRegexFileFilter(Pattern exts, String des, boolean bAcceptDir)
	{
       acceptableExtensions = exts;
       descriptor = des;
       this.bAcceptDir = bAcceptDir;
	}

	@Override
	public boolean accept(File file)
	{
		if (file.isDirectory())
		{
			return bAcceptDir;
		}
		if (acceptableExtensions == null)
		{
			return true;
		}
		return acceptableExtensions.matcher(file.getName()).matches();
	}

	public String getDescription()
	{
		return descriptor;
	}

}