/*
 * Copyright (c) 2018-2021 Karlatemp. All rights reserved.
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 *
 * LuckPerms-Mirai/LuckPerms-Mirai.main/RCP.java
 *
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/Karlatemp/LuckPerms-Mirai/blob/master/LICENSE
 */

package io.github.karlatemp.luckperms.mirai.cp;

import io.github.karlatemp.unsafeaccessor.Root;
import me.lucko.luckperms.common.plugin.bootstrap.LuckPermsBootstrap;
import me.lucko.luckperms.common.plugin.classpath.ClassPathAppender;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class RCP implements ClassPathAppender {
    private static final Method ADD_URL_METHOD;

    static {

        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            Root.openAccess(ADD_URL_METHOD);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final URLClassLoader classLoader;

    public RCP(LuckPermsBootstrap bootstrap) throws IllegalStateException {
        ClassLoader classLoader = bootstrap.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = (URLClassLoader) classLoader;
        } else {
            throw new IllegalStateException("ClassLoader is not instance of URLClassLoader");
        }
    }

    @Override
    public void addJarToClasspath(Path file) {
        try {
            ADD_URL_METHOD.invoke(this.classLoader, file.toUri().toURL());
        } catch (ReflectiveOperationException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
