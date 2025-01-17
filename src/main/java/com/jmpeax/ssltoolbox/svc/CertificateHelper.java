package com.jmpeax.ssltoolbox.svc;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The CertificateHelper class provides utility methods for working with X.509 certificates.
 * <p>
 * This class contains the following methods:
 * <p>
 * 1. getCertificate - This static method takes an InputStream containing a certificate and returns a Set of X509Certificates. It uses the X.509 CertificateFactory to generate the
 * certificates from the input stream.
 * <p>
 * 2. getCommonName - This static method takes an X509Certificate and returns the Common Name (CN) from the subject of the certificate. It uses regular expressions to extract the
 * CN from the subject's name.
 * <p>
 * 3. isValid - This static method takes an X509Certificate and checks if it is valid. It uses the checkValidity() method of the certificate to perform the validation.
 * <p>
 * Please note that this class relies on a logger instance from the Logger class for logging purposes.
 */
@Service()
public final class CertificateHelper {
    private static final Logger LOGGER = Logger.getInstance(CertificateHelper.class);


    /**
     * Retrieves a set of X.509 certificates from the given input stream.
     *
     * @param certificateInput the input stream from which to retrieve the certificates
     * @return a set of X.509 certificates obtained from the input stream
     */
    public @NotNull Set<X509Certificate> getCertificate(@NotNull VirtualFile certificateInput) {
        try (var is = certificateInput.getInputStream()) {
            var fac = CertificateFactory.getInstance("X.509");
            return fac.generateCertificates(is).stream().map(c -> (X509Certificate) c).collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException | CertificateException e) {
            LOGGER.error("Unable to create Certification Factory", e);
        }
        return Set.of();
    }

    /**
     * Retrieves the Common Name (CN) from the subject of the given X509Certificate.
     *
     * @param certificate the X509Certificate from which to retrieve the Common Name
     * @return the Common Name (CN) extracted from the subject of the certificate
     */
    @NotNull
    public String getCommonName(@NotNull X509Certificate certificate) {
        X500Principal principal = certificate.getSubjectX500Principal();
        String subject = principal.getName();
        Pattern pattern = Pattern.compile("CN=([^,]*)");
        Matcher matcher = pattern.matcher(subject);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Checks if the given X509Certificate is valid.
     *
     * @param certificate the X509Certificate to be checked for validity
     * @return true if the certificate is valid, false otherwise
     */
    public boolean isValid(@NotNull X509Certificate certificate) {
        try {
            certificate.checkValidity();
            return true;
        } catch (Exception e) {
            LOGGER.debug("Certificate is not valid", e);
        }
        return false;
    }

    /**
     * Retrieves a map of X.509 certificates from the given keystore input stream.
     *
     * @param keystoreIS       the input stream from which to load the keystore
     * @param keystorePassword the password used to protect the integrity of the keystore
     * @return a map where the keys are certificate aliases and the values are X.509 certificates
     */
    public @NotNull Map<String, X509Certificate> getKeyStoreCerts(@NotNull InputStream keystoreIS, char[] keystorePassword) {
        Map<String, X509Certificate> certs = new LinkedHashMap<>();
        try {
            var keystore = openKeyStore(keystoreIS, keystorePassword);
            Enumeration<String> aliases = keystore.aliases();
            aliases.asIterator().forEachRemaining(alias -> {
                try {
                    Certificate cert = keystore.getCertificate(alias);
                    if (cert instanceof X509Certificate) {
                        certs.put(alias, (X509Certificate) cert);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error loading certificate", e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error loading keystore", e);
        }
        return certs;
    }

    /**
     * Opens a keystore from the specified input stream using the provided password.
     *
     * @param keystoreIS       the input stream from which to load the keystore
     * @param keystorePassword the password used to protect the integrity of the keystore
     * @return a loaded KeyStore instance
     * @throws KeyStoreException        if no Provider supports a KeyStoreSpi implementation for the specified type
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded
     * @throws IOException              if there is an I/O or format problem with the keystore data
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found
     */
    private KeyStore openKeyStore(@NotNull InputStream keystoreIS, char[] keystorePassword) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(keystoreIS, keystorePassword);
        return keystore;
    }

    /**
     * Imports a certificate into a keystore file.
     *
     * @param keyStoreFile the keystore file into which the certificate will be imported
     * @param certToImport the certificate file to be imported
     * @param alias        the alias under which the certificate will be stored
     * @param password     the password for the keystore
     * @throws IOException if an I/O error occurs during the operation
     */
    public void importCertificate(@NotNull VirtualFile keyStoreFile, @NotNull VirtualFile certToImport, @NotNull String alias, char[] password) throws IOException {
        try (var keystoreIS = keyStoreFile.getInputStream()) {
            var keystore = openKeyStore(keystoreIS, password);
            var cert = getCertificate(certToImport).stream().findFirst().orElseThrow();
            keystore.setCertificateEntry(alias, cert);
            var out = Files.newOutputStream(keyStoreFile.toNioPath(), StandardOpenOption.WRITE);
            keyStoreFile.refresh(false, false);
            keystore.store(out, password);
        } catch (Exception e) {
            LOGGER.error("Error importing certificate", e);
        }
    }

    /**
     * Exports an X.509 certificate from a specified keystore file using the given alias and password.
     *
     * @param ksVirtualFile the virtual file representing the keystore containing the certificate
     * @param selectedAlias the alias of the certificate to export
     * @param password      the password to access the keystore
     * @return the X.509 certificate associated with the specified alias, or null if an error occurs
     */
    public @Nullable X509Certificate exportCertificate(VirtualFile ksVirtualFile,
                                                       String selectedAlias,
                                                       char[] password) {
        try (var is = ksVirtualFile.getInputStream()) {
            var keystore = openKeyStore(is, password);
            var cert = keystore.getCertificate(selectedAlias);
            return (X509Certificate) cert;
        } catch (Exception e) {
            LOGGER.error("Error exporting certificate", e);
            return null;
        }
    }

    /**
     * Exports a certificate associated with the given alias from a keystore file to a byte stream.
     * If no certificate is found, an empty {@link ByteArrayOutputStream} is returned.
     *
     * @param ksVirtualFile the virtual file representing the keystore containing the certificate
     * @param selectedAlias the alias of the certificate to export
     * @param password the password to access the keystore
     * @return a {@link ByteArrayOutputStream} containing the exported certificate in PEM format,
     *         or an empty {@link ByteArrayOutputStream} if the certificate could not be exported
     */
    public @NotNull ByteArrayOutputStream exportCertificateToByte(VirtualFile ksVirtualFile,
                                                                  String selectedAlias,
                                                                  char[] password) {
        var cert = exportCertificate(ksVirtualFile, selectedAlias, password);
        if (cert == null) {
            return new ByteArrayOutputStream();
        }
        return exportCertificate(cert);
    }

    /**
     * Exports the given X.509 certificate to a {@link ByteArrayOutputStream} in PEM format.
     * If an error occurs during the export process, an empty {@link ByteArrayOutputStream} is returned.
     *
     * @param certificate the X.509 certificate to export
     * @return a {@link ByteArrayOutputStream} containing the exported certificate in PEM format,
     *         or an empty {@link ByteArrayOutputStream} if an error occurs
     */
    public @NotNull ByteArrayOutputStream exportCertificate(@NotNull X509Certificate certificate) {
        try {
            var out = new ByteArrayOutputStream();
            out.write("-----BEGIN CERTIFICATE-----\n".getBytes());
            out.write(Base64.getEncoder().encode(certificate.getEncoded()));
            out.write("\n-----END CERTIFICATE-----\n".getBytes());
            return out;
        } catch (Exception e) {
            LOGGER.error("Error exporting certificate", e);
            return new ByteArrayOutputStream();
        }
    }

    /**
     * Removes a certificate from the specified keystore file.
     *
     * @param ksVirtualFile the virtual file representing the keystore from which the certificate will be removed
     * @param certAlias     the alias of the certificate to be removed
     * @param password      the password used to access the keystore
     */
    public void removeCertificate(VirtualFile ksVirtualFile, String certAlias, char[] password) {
        try (var is = ksVirtualFile.getInputStream();
             var out = Files.newOutputStream(ksVirtualFile.toNioPath(), StandardOpenOption.WRITE)) {
            var keystore = openKeyStore(is, password);
            keystore.deleteEntry(certAlias);
            keystore.store(out, password);
            ksVirtualFile.refresh(false, false);
        } catch (Exception e) {
            LOGGER.error("Error removing certificate", e);
        }
    }
}
