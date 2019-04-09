package org.ds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

public class FileUtils {
    public final static Pattern REGEX_SUPPORTED_FILE_TYPES =
        Pattern.compile("(?i)(.*csv)$");
    public final static String descriptorSupportedFiles =
        "Mission CSV (*.csv)";
    public static final GenericRegexFileFilter supportedFilesAndFoldersFilter = new GenericRegexFileFilter(
        REGEX_SUPPORTED_FILE_TYPES, descriptorSupportedFiles, true);


    public static boolean isSupportedFile(File filePath)
    {
        return isSupportedFile(filePath.getPath());
    }

    public static boolean isSupportedFile(String strFilepath)
    {
        return REGEX_SUPPORTED_FILE_TYPES.matcher(strFilepath).matches();
    }

    public static File[] sortDir1st(File files[])
    {
        return sort(files, -1);
    }

    public static File[] sortFile1st(File files[])
    {
        return sort(files, 1);
    }

    public static File[] sort(File files[], int nFilesFirst)
    {
        if (files == null || files.length == 0)
            return null; //nothing to do!
        final int nRetVal = nFilesFirst;
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            public int compare(File f1, File f2) {
                //don't assume no nulls
                if (f1 == null)
                {
                    if (f2 == null)
                        return 0;
                    else return -1;
                }
                else
                {
                    if (f2 == null)
                        return 1;
                    if (f1.equals(f2))
                        return 0;
                    if (f1.isDirectory())
                    {
                        if (f2.isDirectory())
                            return f1.getName().compareToIgnoreCase(f2.getName());
                        else
                            return nRetVal;	//For directory before files, return -1, else 1
                    }
                    else
                    {
                        if (f2.isDirectory())
                            return -nRetVal;	//For directory before files return 1, else -1
                            //assume files in same folder
                        else return f1.getName().compareToIgnoreCase(f2.getName());
                    }
                }
            }
            public boolean equals(Object obj) {
                return false;
            }
        });

        return (File[]) list.toArray();
    }

    /*
     * expandFolder to get at least one pic or all pics
     */
    public Vector<File> expandFolder(File sourceDir,
                                     boolean bAtLeastOne)
    {
        Vector<File> v = new Vector<File>();
        v.add(sourceDir);
        expandFolder(0, v, bAtLeastOne);
        return v;
    }

    /*
     * expandFolder to get at least one pic or all pics
     *
     * @param nStartIndex	index of folder to expand
     * @param v				container for files, must not be null
     * @param bAtLeastOne	indicate to stop recursing folders when one file is found;
     * 						set to false to force loading of all files.
     */
    public boolean expandFolder(int nStartIndex, Vector<File> v,
                                boolean bAtLeastOne)
    {
        boolean bFoundOne = false;
        while (nStartIndex < v.size())
        {
            File f = v.elementAt(nStartIndex);
            if (f == null)
            {
                v.removeElementAt(nStartIndex);
                continue;
            }
            else if (f.isFile())
            {
                if (bAtLeastOne)
                    return true; //found at least one file, so mission accomplished
                //o.w. continue to expand remaining directories if any
                ++nStartIndex;
                bFoundOne = true;
                continue;
            }
            else //if (f.isDirectory())
            {
                v.removeElementAt(nStartIndex);
                //o.w. try expanding folder
                File files[] = sortFile1st(f.listFiles(supportedFilesAndFoldersFilter));
                //for (File f: files)
                //    System.out.println(f);
                if (files == null || files.length <= 0)
                {
                    continue;
                }
                //add expanded listing of files & folders
                int i = nStartIndex;
                for (File f2: files)
                {
                    v.add(i, f2); //insert
                    ++i;
                    if (f2.isFile())
                        ++nStartIndex;
                }
                //check exit condition
                if (files[0].isFile())
                {
                    bFoundOne = true;
                    if (bAtLeastOne)
                        return true; //found at least one file, so mission accomplished
                }
            }
        }
        return bFoundOne;
    }

    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean copyFile(File sourceFile, File destFile) throws IOException {
        if ((sourceFile == null) || (destFile == null))
            return false;

        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        if (sourceFile.equals(destFile))
            return true; //nothing to do; same file!

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            return true;
        }
        catch (IOException ex) {
            return false;
        }
        finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static String readFile(String path, Charset encoding)
    {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(path));
            return new String(encoded, encoding);
        }
        catch (IOException ex) {
            return "";
        }
    }

    public static String readFile(String path) {
        return readFile(path, StandardCharsets.UTF_8);
    }
}
