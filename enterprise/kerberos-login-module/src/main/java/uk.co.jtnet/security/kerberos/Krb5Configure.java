package uk.co.jtnet.security.kerberos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class Krb5Configure {

	//private static final Logger LOG = LoggerFactory.getLogger(Krb5Configure.class);
	private static Path krb5ConfTmpOutputPath;
	private static String krb5ConfTmpOutputStr;
	private static final CopyOption[] options = new CopyOption[]{
		StandardCopyOption.REPLACE_EXISTING,
		StandardCopyOption.COPY_ATTRIBUTES
	};


	public static void init (String krb5ConfUrlStr) throws IOException, URISyntaxException {
		URI krb5ConfURI = null;
		try {
			krb5ConfURI = new URI(krb5ConfUrlStr);
		} catch (URISyntaxException e) {
			if (krb5ConfURI == null){
				throw new URISyntaxException("Bad syntax for URL.", krb5ConfUrlStr);
			}
			// Do nothing else. Switch statement below handles this, sort of
		}
		System.out.println(krb5ConfURI.getSchemeSpecificPart());
		
		krb5ConfTmpOutputStr = System.getProperty("kerb.krb5Conf.tmp.path", 
				System.getProperty("java.io.tmpdir") + File.separator + "krb5.conf");
		krb5ConfTmpOutputPath = Paths.get(krb5ConfTmpOutputStr);

		switch(krb5ConfURI.getScheme()){
		case "classpathfile":
			//load from classpath where file is on disk not in jar
			copyFromClasspathFile(krb5ConfURI.getSchemeSpecificPart());
			break;
		case "class":
			//load from classpath where file will be in jar
			copyFromJar(krb5ConfURI.getSchemeSpecificPart());
			break;
		case "file":
			krb5ConfTmpOutputStr = krb5ConfURI.getSchemeSpecificPart();
			break;
		case "http":
			copyFromHttpUri(krb5ConfURI);
			break;
		case "https":
			//Site's certificate or the CA of this certificate needs to be in the truststore configured for the JVM process.
			copyFromHttpUri(krb5ConfURI);
			break;
		default:
			throw new URISyntaxException("Unsupported protocol.", krb5ConfUrlStr);
		}
		//Finally set the system property.
		System.setProperty("java.security.krb5.conf", krb5ConfTmpOutputStr);
		//LOG.debug("Using krb5.conf: " + krb5ConfUrlStr + " storing at location: " + krb5ConfTmpOutputStr);

	}

	private static void copyFromJar(String krb5ClasspathLocation) throws IOException{
		InputStream input = Krb5Configure.class.getClassLoader().getResourceAsStream(krb5ClasspathLocation);
        File resDestFile = new File(krb5ConfTmpOutputStr);
        FileOutputStream output = null;
        try {
        	output = new FileOutputStream(resDestFile);
            int readBytes;
            byte[] buffer = new byte[1024];
            while ((readBytes = input.read(buffer)) > 0) {
            	output.write(buffer, 0, readBytes);
            }
        } finally {
        	output.close();
        	input.close();
        }
	}

	private static void copyFromClasspathFile(String krb5ConfFileName) throws IOException{
		Path inputPath = Paths.get(Krb5Configure.class.getClassLoader().getResource(krb5ConfFileName).getPath());
		Files.copy(inputPath, krb5ConfTmpOutputPath, options);
	}

	private static void copyFromHttpUri(URI krb5ConfURI) throws IOException {
		URL krb5ConfURL = krb5ConfURI.toURL();
		URLConnection UrlConn = krb5ConfURL.openConnection();
		UrlConn.connect();
		InputStream input = null;
		FileOutputStream output = null;
		try {
			input = UrlConn.getInputStream();
			output = new FileOutputStream(krb5ConfTmpOutputStr);
			int readBytes;
			byte[] buffer = new byte[1024];
			while ((readBytes = input.read(buffer)) > 0) {
				output.write(buffer, 0, readBytes);
			}
		} finally {
			input.close();
			output.close();
		}
	}

}
