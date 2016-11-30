package com.hazelcast.samples.spring.data.migration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.shell.Bootstrap;

/**
 * <P>Run the translater much as before, using the command line to invoke
 * the translation service.
 * </P>
 * <P>Use <A HREF="https://projects.spring.io/spring-shell">Spring Shell</A>
 * for command line handling as it's more appropriate than
 * <A HREF="http://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-remote-shell.html">CRaSH</A>
 * although not as nicely integrated yet.
 * </P>
 * @see <A HREF="https://github.com/spring-projects/spring-shell/issues/34">
 * 	https://github.com/spring-projects/spring-shell/issues/34</A> 
 * <P><U><B>MIGRATION PATH</B></U></P>
 * <OL>
 * <LI>Duplicate this class from JPA repository, or move to a central module.
 * </LI>
 * </OL>
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class AfterTranslator {

    public static void main(String[] args) throws Exception {
    	Bootstrap.main(args);
    }

}
