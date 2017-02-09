package com.gft.lr.restcheck;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.NoSuchElementException;

/**
 * Created on 09/02/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectClassLoaderTest {

    public static final String LOGIN_CONTROLLER_CLASS_NAME = "controllers.LoginController";
    public static final String TEST_JAR = "src/test/resources/test.jar";

    @Test
    public void shouldLoadClass() throws Exception {
        // given
        ProjectClassLoader projectClassLoader = new ProjectClassLoader(this.getClass().getClassLoader(), TEST_JAR);

        // when
        Class<?> loadedClass = projectClassLoader.findClass(LOGIN_CONTROLLER_CLASS_NAME);

        // then
        Assertions.assertThat(loadedClass.getCanonicalName()).isEqualTo(LOGIN_CONTROLLER_CLASS_NAME);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldNotLoadClass() throws Exception {
        // given
        ProjectClassLoader projectClassLoader = new ProjectClassLoader(this.getClass().getClassLoader(), TEST_JAR);

        // when
        projectClassLoader.findClass("never.KnownController");
    }
}
