package me.carboncrab.enumtojson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipInputStream;

public class Main {

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        File localDirectory = new File(getLocalDirectory());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jarName = getInput("Enter name of target jar").replaceAll(".jar", "") + ".jar";
        File jarFile = new File(localDirectory.getAbsolutePath() + "\\" + jarName);
        if(!jarFile.exists()) {
            System.out.println("Error: File not found.");
            return;
        }
        String enumClassName = getInput("Enter name of enum class");
        Set<Class<?>> classes = getAllClassesInJar(jarFile);
        Class<?> targetClass = classes.stream().filter(c -> c.getName().equalsIgnoreCase(enumClassName)).findFirst().orElse(null);
        if(targetClass == null) {
            System.out.println("Could not find specified class! Make sure the formatting is correct (Example: me.example.package.EnumClass).");
            return;
        }
        if(!targetClass.isEnum()) {
            System.out.println("Specified class is not an enum!");
            return;
        }
        Object[] values = targetClass.getEnumConstants();
        String json = gson.toJson(values);
        String outputName = getInput("Enter name of JSON file").replaceAll(".json", "") + ".json";
        File jsonFile = new File(localDirectory.getAbsolutePath() + "\\" + outputName);
        boolean result = writeFile(jsonFile, json);
        if(result) System.out.println("Successfully created JSON file.");
        else System.out.println("Failed to create JSON file.");
    }

    private static String getLocalDirectory() {
        try {
            return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getPath();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static Set<Class<?>> getAllClassesInJar(File jarFile) {
        try {
            URL jar = jarFile.toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[]{jar}, Main.class.getClassLoader());
            JarInputStream jarInput = new JarInputStream(jar.openStream());

            JarEntry entry;
            Set<Class<?>> classes = new HashSet<>();
            while ((entry = jarInput.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (!name.endsWith(".class")) continue;
                name = name.substring(0, name.lastIndexOf('.')).replace('/', '.');
                classes.add(loader.loadClass(name));
            }
            if (classes.isEmpty()) {
                loader.close();
                return new HashSet<>();
            }
            return classes;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    private static boolean writeFile(File file, String str) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(str);
            writer.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String getInput(String prompt) {
        System.out.print(prompt + ": ");
        return SCANNER.nextLine();
    }

}
