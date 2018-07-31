package com.android.jmaxime.factory.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see OkHttpConfiguration
 * @see ApiFactory
 * @see OkHttpBuilder
 */
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiKeyService {
    /**
     * e.g.: "cube_assembler", "stores", "interact" ...
     *
     * @return la key name de la api cible
     */
    String apiKeyName() default "";

    /**
     * e.g. "proxy_v3", "cube", "integ", "stores", "open_voice" ...
     *
     * @return la clé de la base url cible
     */
    String baseUrlName();

    /**
     * Allows to give a name to the API if the base url and / or api key name is not used
     *
     * @return
     */
    String tagApiName() default "";

    /**
     * Set the time out
     * -1: default
     * n: custom value
     * @see OkHttpBuilder
     * @return time out value, default returns -1 to let the OkHttpBuilder set the default (usually 30s)
     */
    int timeOut() default -1;

    /**
     * Cette valeur sera retourner à la Factory HttpClient
     *
     * @return false default value
     */
    boolean addCache() default false;

    /**
     * Cette valeur sera retourner à la Factory HttpClient
     *
     * @return false default value
     */
    boolean allowCookies() default false;
}
