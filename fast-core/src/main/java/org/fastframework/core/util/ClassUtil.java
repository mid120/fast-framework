package org.fastframework.core.util;

import org.apache.commons.lang3.StringUtils;
import org.fastframework.mvc.interceptor.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 类扫描工具类
 *
 * Created by bysocket on 16/7/20.
 */
public class ClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);

    /** 包目录分隔符:点 */
    private static final String PACKAGE_PATH_POINT        = ".";
    /** 包目录分隔符:正斜线 */
    private static final String PACKAGE_PATH_SEPARATOR    = "/";

    /** URL 协议名:file */
    private static final String URL_PROTOCOL_FILE         = "file";
    /** URL 协议名:jar */
    private static final String URL_PROTOCOL_JAR          = "jar";

    /** class 文件名后缀 */
    private static final String CLASS_FILE_END            = ".class";
    /** class 文件分隔符:点 */
    private static final String CLASS_FILE_POINT          = PACKAGE_PATH_POINT;

    /** 空格的unicode编码 */
    private static final String UNICODE_SPACE             = "%20";

    /** 当前线程的类加载 */
    private static final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

    private static ClassLoader getCurrentClassLoader() {
        return currentClassLoader;
    }

    /**
     * 根据包名和注解,获取包下的有该注解的类列表
     *
     * @param packageName
     * @param annotationClass
     * @return
     */
    public static List<Class<?>> getClassListByAnnotation(String packageName, Class<? extends Annotation> annotationClass) {
        List<Class<?>> classList     = getClassList(packageName);
        return classList.stream().filter(clazz ->
                clazz.isAnnotationPresent(annotationClass)).collect(Collectors.toList());
    }

    /**
     * 根据包名和接口, 获取实现了该接口的类列表
     *
     * @param packageName
     * @param handlerInterceptorClass
     * @return
     */
    public static List<Class<?>> getClassListByInterface(String packageName, Class<HandlerInterceptor> handlerInterceptorClass) {
        List<Class<?>> classes = getClassList(packageName);
        return classes.stream().filter(clazz ->
                handlerInterceptorClass.isAssignableFrom(clazz) && !handlerInterceptorClass.equals(clazz))
                .collect(Collectors.toList());
    }

    /**
     * 根据包名,获取包下的类列表
     *
     * @param packageName
     * @return
     */
    private static List<Class<?>> getClassList(String packageName) {
        List<Class<?>> classList = new ArrayList<>();
        try {
            // 从包名获取 URL 类型的资源(将包名中的'.'替换成'/')
            Enumeration<URL> urlList = getCurrentClassLoader()
                    .getResources(packageName.replace(PACKAGE_PATH_POINT, PACKAGE_PATH_SEPARATOR));
            while(urlList.hasMoreElements()) {
                URL url = urlList.nextElement();
                if (url != null) {
                    // 获取 URL 协议名 [file] [jar]
                    String protocol = url.getProtocol();
                    if (StringUtils.equals(URL_PROTOCOL_FILE, protocol)) {
                        // 若在 class 目录,添加类
                        String packagePath = url.getPath().replaceAll(UNICODE_SPACE, " ");
                        addClass(classList, packagePath, packageName);
                    } else if (StringUtils.equals(URL_PROTOCOL_JAR, protocol)) {
                        // TODO 若在 jar 包中,则解析 jar 包中的 entry
                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        JarFile jarFile = jarURLConnection.getJarFile();
                        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                        while(jarEntryEnumeration.hasMoreElements()) {
                            JarEntry jarEntry   = jarEntryEnumeration.nextElement();
                            String jarEntryName = jarEntry.getName();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取扫描类错误, error:" + e);
        }
        return classList;
    }

    /**
     * 根据 包路径 和 包名称,添加类到类列表(递归扫描)
     *
     * @param classList
     * @param packagePath
     * @param packageName
     */
    @SuppressWarnings("unchecked")
    private static void addClass(List<Class<?>> classList, String packagePath, String packageName) {
        try {
            // 获取 包路径 下的所有 class文件列表或目录
            List<File> files = new LinkedList<>();
            File dictionary = new File(packagePath);
            if(dictionary.isFile()) {
                files.add(dictionary);
            }
            else{
                SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        files.add(file.toFile());
                        return super.visitFile(file, attrs);
                    }
                };
                Files.walkFileTree(Paths.get(packagePath), finder);
            }
            // 遍历文件或目录
            files.forEach(file -> {
                String absolutePath = file.getAbsolutePath().replace(PACKAGE_PATH_SEPARATOR, PACKAGE_PATH_POINT);
                String className = absolutePath.substring(absolutePath.lastIndexOf(packageName), absolutePath.lastIndexOf(PACKAGE_PATH_POINT));
                Class<?> clazz = loadClass(className,false);
                classList.add(clazz);
            });
        } catch (Exception e) {
            LOGGER.error("添加类错误");
        }
    }

    /**
     * 加载类
     *
     * @param className
     * @param initialize
     * @return
     */
    private static Class<?> loadClass(String className,Boolean initialize) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className,initialize,getCurrentClassLoader());
            LOGGER.info("加载类: " + clazz.getName());
        } catch (ClassNotFoundException e) {
            LOGGER.error("加载类错误");
            throw new RuntimeException(e);
        }
        return clazz;
    }
}