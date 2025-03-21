package com.stericson.rootshell.containers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* #ANNOTATIONS @SupportedAnnotationTypes("com.stericson.rootshell.containers.RootClass.Candidate") */
/* #ANNOTATIONS @SupportedSourceVersion(SourceVersion.RELEASE_6) */
public class RootClass /* #ANNOTATIONS extends AbstractProcessor */ {

    /* #ANNOTATIONS
    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "I was invoked!!!");

        return false;
    }
    */

    @SuppressWarnings("unused")
    static volatile String PATH_TO_DX = "/Users/Chris/Projects/android-sdk-macosx/build-tools/18.0.1/dx";

    enum READ_STATE {
        STARTING, FOUND_ANNOTATION
    }

    public RootClass(String[] args) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException {

        // Note: rather than calling System.load("/system/lib/libandroid_runtime.so");
        // which would leave a bunch of unresolved JNI references,
        // we are using the 'withFramework' class as a preloader.
        // So, yeah, russian dolls: withFramework > RootClass > actual method

        String className = args[0];
        RootArgs actualArgs = new RootArgs();
        actualArgs.args = new String[args.length - 1];
        System.arraycopy(args, 1, actualArgs.args, 0, args.length - 1);
        Class<?> classHandler = Class.forName(className);
        Constructor<?> classConstructor = classHandler.getConstructor(RootArgs.class);
        classConstructor.newInstance(actualArgs);
    }

    @SuppressWarnings("unused")
    public @interface Candidate {

    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class RootArgs {

        String[] args;
    }

    static void displayError(Exception e) {
        // Not using system.err to make it easier to capture from
        // calling library.
        System.out.println("##ERR##" + e.getMessage() + "##");
        e.printStackTrace();
    }

    // I reckon it would be better to investigate classes using getAttribute()
    // however this method allows the developer to simply select "Run" on RootClass
    // and immediately re-generate the necessary jar file.
    static public class AnnotationsFinder {

        private final String AVOIDDIRPATH = "stericson" + File.separator + "rootshell" + File.separator;

        @SuppressWarnings("FieldCanBeLocal")
        private final List<File> classFiles;

        @SuppressWarnings("TryWithIdenticalCatches")
        public AnnotationsFinder() throws IOException {
            System.out.println("Discovering root class annotations...");
            classFiles = new ArrayList<>();
            lookup(new File("src"), classFiles);
            System.out.println("Done discovering annotations. Building jar file.");
            File builtPath = getBuiltPath();
            if (null != builtPath) {
                // Android! Y U no have com.google.common.base.Joiner class?
                String rc1 = "com" + File.separator
                        + "stericson" + File.separator
                        + "rootshell" + File.separator
                        + "containers" + File.separator
                        + "RootClass.class";
                String rc2 = "com" + File.separator
                        + "stericson" + File.separator
                        + "rootshell" + File.separator
                        + "containers" + File.separator
                        + "RootClass$RootArgs.class";
                String rc3 = "com" + File.separator
                        + "stericson" + File.separator
                        + "rootshell" + File.separator
                        + "containers" + File.separator
                        + "RootClass$AnnotationsFinder.class";
                String rc4 = "com" + File.separator
                        + "stericson" + File.separator
                        + "rootshell" + File.separator
                        + "containers" + File.separator
                        + "RootClass$AnnotationsFinder$1.class";
                String rc5 = "com" + File.separator
                        + "stericson" + File.separator
                        + "rootshell" + File.separator
                        + "containers" + File.separator
                        + "RootClass$AnnotationsFinder$2.class";
                String[] cmd;
                @SuppressWarnings({"IndexOfReplaceableByContains", "ConstantConditions"})
                boolean onWindows = (-1 != System.getProperty("os.name").toLowerCase().indexOf("win"));
                if (onWindows) {
                    StringBuilder sb = new StringBuilder(
                            " " + rc1 + " " + rc2 + " " + rc3 + " " + rc4 + " " + rc5
                    );
                    for (File file : classFiles) {
                        //noinspection StringConcatenationInsideStringBufferAppend
                        sb.append(" " + file.getPath());
                    }
                    cmd = new String[]{
                            "cmd", "/C",
                            "jar cvf" +
                                    " anbuild.jar" +
                                    sb//.toString()
                    };
                } else {
                    ArrayList<String> al = new ArrayList<>();
                    al.add("jar");
                    al.add("cf");
                    al.add("anbuild.jar");
                    al.add(rc1);
                    al.add(rc2);
                    al.add(rc3);
                    al.add(rc4);
                    al.add(rc5);
                    for (File file : classFiles) {
                        al.add(file.getPath());
                    }
                    //noinspection ToArrayCallWithZeroLengthArrayArgument
                    cmd = al.toArray(new String[al.size()]);
                }
                ProcessBuilder jarBuilder = new ProcessBuilder(cmd);
                jarBuilder.directory(builtPath);
                try {
                    jarBuilder.start().waitFor();
                } catch (IOException ignored) {
                } catch (InterruptedException ignored) {
                }

                String strRawFolder; // = "res" + File.separator + "raw";
                //noinspection StatementWithEmptyBody
                if (builtPath.toString().startsWith("build")); //Check if running in AndroidStudio
                strRawFolder = "src" + File.separator + "main" + File.separator + "res" + File.separator + "raw";

                File rawFolder = new File(strRawFolder);
                if (!rawFolder.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    rawFolder.mkdirs();
                }

                System.out.println("Done building jar file. Creating dex file.");
                if (onWindows) {
                    cmd = new String[]{
                            "cmd", "/C",
                            "dx --dex --output=" + strRawFolder + File.separator + "anbuild.dex "
                                    + builtPath + File.separator + "anbuild.jar"
                    };
                } else {
                    cmd = new String[]{
                            getPathToDx(),
                            "--dex",
                            "--output=" + strRawFolder + File.separator + "anbuild.dex",
                            builtPath + File.separator + "anbuild.jar"
                    };
                }
                ProcessBuilder dexBuilder = new ProcessBuilder(cmd);
                try {
                    dexBuilder.start().waitFor();
                } catch (IOException ignored) {
                } catch (InterruptedException ignored) {
                }
            }
            System.out.println("All done. ::: anbuild.dex should now be in your project's src" + File.separator + "main" + File.separator + "res" + File.separator + "raw" + File.separator + " folder :::");
        }

        protected void lookup(File path, List<File> fileList) {
            String desourcedPath = path.toString().replace("src" + File.separator, "").replace("main" + File.separator + "java" + File.separator, "");

            File[] files = path.listFiles(file -> true);
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        //noinspection IndexOfReplaceableByContains
                        if (-1 == file.getAbsolutePath().indexOf(AVOIDDIRPATH)) {
                            lookup(file, fileList);
                        }
                    } else {
                        if (file.getName().endsWith(".java")) {
                            if (hasClassAnnotation(file)) {
                                final String fileNamePrefix = file.getName().replace(".java", "");
                                final File compiledPath = new File(getBuiltPath().toString() + File.separator + desourcedPath);
                                File[] classAndInnerClassFiles = compiledPath.listFiles((dir, filename) -> filename.startsWith(fileNamePrefix));
                                if (classAndInnerClassFiles != null) {
                                    for (final File matchingFile : classAndInnerClassFiles) {
                                        fileList.add(new File(desourcedPath + File.separator + matchingFile.getName()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @SuppressWarnings("TryWithIdenticalCatches")
        protected boolean hasClassAnnotation(File file) {
            READ_STATE readState = READ_STATE.STARTING;
            Pattern p = Pattern.compile(" class ([A-Za-z0-9_]+)");
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while (null != (line = reader.readLine())) {
                    switch (readState) {
                        case STARTING:
                            //noinspection IndexOfReplaceableByContains
                            if (-1 < line.indexOf("@RootClass.Candidate")) {
                                readState = READ_STATE.FOUND_ANNOTATION;
                            }
                            break;
                        case FOUND_ANNOTATION:
                            Matcher m = p.matcher(line);
                            if (m.find()) {
                                System.out.println(" Found annotated class: " + m.group(0));
                                return true;
                            } else {
                                System.err.println("Error: unmatched annotation in " +
                                        file.getAbsolutePath());
                                readState = READ_STATE.STARTING;
                            }
                            break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected String getPathToDx() throws IOException {
            String androidHome = System.getenv("ANDROID_HOME");
            if (null == androidHome) {
                throw new IOException("Error: you need to set $ANDROID_HOME globally");
            }
            String dxPath = null;
            File[] files = new File(androidHome + File.separator + "build-tools").listFiles();
            int recentSdkVersion = 0;
            if (files != null) {
                for (File file : files) {

                    String fileName; // = null;
                    if (file.getName().contains("-")) {
                        String[] splitFileName = file.getName().split("-");
                        if (splitFileName[1].contains("W")) {
                            char[] fileNameChars = splitFileName[1].toCharArray();
                            fileName = String.valueOf(fileNameChars[0]);
                        } else if (splitFileName[1].contains("rc")) {
                            continue; //Do not use release candidates
                        } else {
                            fileName = splitFileName[1];
                        }
                    } else {
                        fileName = file.getName();
                    }

                    int sdkVersion;

                    String[] sdkVersionBits = fileName.split("[.]");
                    sdkVersion = Integer.parseInt(sdkVersionBits[0]) * 10000;
                    if (sdkVersionBits.length > 1) {
                        sdkVersion += Integer.parseInt(sdkVersionBits[1]) * 100;
                        if (sdkVersionBits.length > 2) {
                            sdkVersion += Integer.parseInt(sdkVersionBits[2]);
                        }
                    }
                    if (sdkVersion > recentSdkVersion) {
                        String tentativePath = file.getAbsolutePath() + File.separator + "dx";
                        if (new File(tentativePath).exists()) {
                            recentSdkVersion = sdkVersion;
                            dxPath = tentativePath;
                        }
                    }
                }
            }
            if (dxPath == null) {
                throw new IOException("Error: unable to find dx binary in $ANDROID_HOME");
            }
            return dxPath;
        }

        protected File getBuiltPath() {
            File foundPath = null;

            File ideaPath = new File("out" + File.separator + "production"); // IntelliJ
            if (ideaPath.isDirectory()) {
                File[] children = ideaPath.listFiles(File::isDirectory);
                if ((children != null) && children.length > 0) {
                    foundPath = new File(ideaPath.getAbsolutePath() + File.separator + children[0].getName());
                }
            }
            if (null == foundPath) {
                File eclipsePath = new File("bin" + File.separator + "classes"); // Eclipse IDE
                if (eclipsePath.isDirectory()) {
                    foundPath = eclipsePath;
                }
            }
            if (null == foundPath) {
                File androidStudioPath = new File("build" + File.separator + "intermediates" + File.separator + "classes" + File.separator + "debug"); // Android Studio
                if (androidStudioPath.isDirectory()) {
                    foundPath = androidStudioPath;
                }
            }

            return foundPath;
        }


    }

    @SuppressWarnings("InstantiationOfUtilityClass")
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                new AnnotationsFinder();
            } else {
                new RootClass(args);
            }
        } catch (Exception e) {
            displayError(e);
        }
    }
}