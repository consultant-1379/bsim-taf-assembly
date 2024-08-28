/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.handlers.templates;

import com.ericsson.oss.bsim.handlers.templates.domain.TemplateCommand;

/**
 * @author ecilosh
 */
public interface TemplateCommandBuilder {

    /**
     * Create a {@link TemplateCommand} from this builder.
     * 
     * @return A {@link TemplateCommand} object constructed from this builder.
     */
    TemplateCommand build();

    /**
     * Add a create command
     * This must be called first followed by one or more parameter calls
     * <p>
     * Example, <blockquote>
     * <code>TemplateCommandBuilder.builder().create().name("name").domain("domain").type("type").file("file").desc("desc").build();</code>
     * </blockquote>
     * </p>
     * 
     * @return This Builder object to allow for chaining of calls.
     */
    public TemplateCommandBuilder create();

    /**
     * Add a delete command
     * This must be called first followed by the name parameter
     * <p>
     * Example, <blockquote> <code>TemplateCommandBuilder.builder().delete().name("name").build();</code> </blockquote>
     * </p>
     * 
     * @return This Builder object to allow for chaining of calls.
     */
    public TemplateCommandBuilder delete();

    /**
     * Add a findall command
     * <p>
     * Example, <blockquote> <code>TemplateCommandBuilder.builder().findall().build();</code> </blockquote>
     * </p>
     * 
     * @return This Builder object to allow for chaining of calls.
     */
    public TemplateCommandBuilder findall();

    /**
     * Add a name parameter
     * 
     * @param name
     *        The name string to be included in the command
     * @return This Builder object to allow for chaining of calls.
     */
    public TemplateCommandBuilder name(String name);

    /**
     * Add a domain parameter
     * 
     * @param domain
     *        The domain string to be included in the command
     * @return This Builder object to allow for chaining of calls.
     */
    public TemplateCommandBuilder domain(String domain);

    /**
     * Add a type parameter
     * 
     * @param type
     *        The type string to be included in the command
     * @return This Builder object to allow for chaining of calls.
     */
    public TemplateCommandBuilder type(String type);

    /**
     * Add a file parameter
     * 
     * @param file
     *        The file string to be included in the command
     * @return This Builder object to allow for chaining of calls.
     */
    public TemplateCommandBuilder file(String file);

    /**
     * Add a description parameter
     * 
     * @param desc
     *        The description string to be included in the command
     * @return This Builder object to allow for chaining of calls.
     */
    public TemplateCommandBuilder desc(String desc);

}
