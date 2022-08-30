/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.services.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.text.StrTokenizer;

public class MetadataScript {

    private final String metadataKey;
    private final String valueSource;
    private final String typeTarget;
    private final String variable;
    private final List<String> values = new ArrayList<>();

    /**
     * Creates a MetadataScript with given command.
     * @param command the given command.
     */
    public MetadataScript(String command) {
        Map<String, String> parameters = new HashMap<>();
        StrTokenizer tokenizer = new StrTokenizer(command, ' ', '\"');
        while (tokenizer.hasNext()) {
            String tok = tokenizer.nextToken();
            if (Objects.nonNull(tok) && tok.contains(":")) {
                String key = tok.substring(0, tok.indexOf(':'));
                String value = tok.substring(tok.indexOf(':') + 1);
                parameters.put(key, value);
            }
        }

        metadataKey = parameters.get("key");
        if (Objects.nonNull(parameters.get("value"))) {
            values.add(parameters.get("value"));
        }
        valueSource = parameters.get("source");
        typeTarget = parameters.get("type");
        variable = parameters.get("variable");

    }

    /**
     * Get goal.
     * @return goal.
     */
    public String getMetadataKey() {
        return metadataKey;
    }

    /**
     * Get source of value.
     * @return source.
     */
    public String getValueSource() {
        return valueSource;
    }

    /**
     * Get values.
     * @return values.
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * Get typeTarget.
     * @return typeTarget
     */
    public String getTypeTarget() {
        return typeTarget;
    }

    public String getVariable() {
        return variable;
    }

}
