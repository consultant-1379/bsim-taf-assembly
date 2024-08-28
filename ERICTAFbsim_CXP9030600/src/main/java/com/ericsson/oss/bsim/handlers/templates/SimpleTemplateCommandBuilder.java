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
public class SimpleTemplateCommandBuilder implements TemplateCommandBuilder {

    private static final String NAME_PREFIX = "-n";

    private static final String DOMAIN_PREFIX = "-d";

    private static final String CREATE = "create";

    private static final String DELETE = "delete";

    private static final String FINDALL = "findall";

    private static final String TYPE_PREFIX = "-t";

    private static final String DESC_PREFIX = "-desc";

    private static final String FILE_PREFIX = "-f";

    private final StringBuilder templateCommand = new StringBuilder("/opt/ericsson/nms_us_templates_cli/bin/ustemplates.sh");

    private SimpleTemplateCommandBuilder() {
    }

    public static TemplateCommandBuilder builder() {
        return new SimpleTemplateCommandBuilder();
    }

    @Override
    public TemplateCommandBuilder domain(final String domain) {
        templateCommand.append(String.format(" %1$s %2$s", DOMAIN_PREFIX, domain));
        return this;
    }

    @Override
    public TemplateCommandBuilder name(final String name) {
        templateCommand.append(String.format(" %1$s %2$s", NAME_PREFIX, name));
        return this;
    }

    @Override
    public TemplateCommandBuilder type(final String type) {
        templateCommand.append(String.format(" %1$s %2$s", TYPE_PREFIX, type));
        return this;
    }

    @Override
    public TemplateCommandBuilder file(final String file) {
        templateCommand.append(String.format(" %1$s %2$s", FILE_PREFIX, file));
        return this;
    }

    @Override
    public TemplateCommandBuilder desc(final String desc) {
        templateCommand.append(String.format(" %1$s %2$s", DESC_PREFIX, desc));
        return this;
    }

    @Override
    public TemplateCommandBuilder create() {
        templateCommand.append(String.format(" %1$s", CREATE));
        return this;
    }

    @Override
    public TemplateCommandBuilder delete() {
        templateCommand.append(String.format(" %1$s", DELETE));
        return this;
    }

    @Override
    public TemplateCommandBuilder findall() {
        templateCommand.append(String.format(" %1$s", FINDALL));
        return this;
    }

    @Override
    public TemplateCommand build() {
        final String builtCommand = this.templateCommand.toString();
        return new TemplateCommand(builtCommand);
    }

}
