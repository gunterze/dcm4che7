package org.dcm4che7;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since May 2021
 */
public abstract class ElementDictionary {
    private static final ServiceLoader<ElementDictionary> loader = ServiceLoader.load(ElementDictionary.class);
    private static final Map<String, ElementDictionary> privateDictionaries = new HashMap<>();
    private final String privateCreator;
    private final Class<?> tagClass;

    protected ElementDictionary(String privateCreator, Class<?> tagClass) {
        this.privateCreator = privateCreator;
        this.tagClass = tagClass;
    }

    public static ElementDictionary standardElementDictionary() {
        return StandardElementDictionary.INSTANCE;
    }

    public static ElementDictionary elementDictionaryOf(String privateCreator) {
        return privateCreator == null ? StandardElementDictionary.INSTANCE
                : privateDictionaries.computeIfAbsent(privateCreator, ElementDictionary::loadPrivateDictionary);
    }

    private static ElementDictionary loadPrivateDictionary(String privateCreator) {
        return loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(x -> privateCreator.equals(x.getPrivateCreator()))
                .findAny()
                .orElse(StandardElementDictionary.INSTANCE);
    }

    public static void reload() {
        synchronized (loader) {
            loader.reload();
        }
    }

    public static VR vrOf(int tag, String privateCreator) {
        return elementDictionaryOf(privateCreator).vrOf(tag);
    }

    public static String keywordOf(int tag, String privateCreator) {
        return elementDictionaryOf(privateCreator).keywordOf(tag);
    }

    public static int tagForKeyword(String keyword, String privateCreatorID) {
        return elementDictionaryOf(privateCreatorID).tagForKeyword(keyword);
    }

    public final String getPrivateCreator() {
        return privateCreator;
    }

    public abstract VR vrOf(int tag);

    public abstract String keywordOf(int tag);

    public int tmTagOf(int daTag) {
        return 0;
    }

    public int daTagOf(int tmTag) {
        return 0;
    }

    public int tagForKeyword(String keyword) {
        if (tagClass != null)
            try {
                return tagClass.getField(keyword).getInt(null);
            } catch (Exception ignore) { }
        return -1;
    }
}
