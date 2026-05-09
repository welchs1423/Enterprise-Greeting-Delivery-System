package com.egds.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Wraps plain text content in a serialized Vue 3 Virtual DOM node tree
 * before it enters the EGDS output channel.
 *
 * <p>The produced JSON represents a minimal VNode graph with a root
 * {@code div#egds-app} element and a single {@code span} child carrying
 * the greeting text as a leaf text node.
 */
@Component
public class Vue3VirtualDomRenderer {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(Vue3VirtualDomRenderer.class);

    /** shapeFlag for elements whose children is a single text string. */
    private static final int SHAPE_FLAG_TEXT_CHILDREN = 8;

    /** shapeFlag for elements whose children is an array of VNodes. */
    private static final int SHAPE_FLAG_ARRAY_CHILDREN = 16;

    /** patchFlag indicating the element contains dynamic text. */
    private static final int PATCH_FLAG_TEXT = 1;

    /**
     * Serializes {@code textContent} as a Vue 3 VNode tree JSON string.
     * Escapes backslashes and double-quotes in the input before embedding.
     *
     * @param textContent the plain text to embed; must not be null
     * @return JSON string of the VNode tree rooted at a {@code div}
     */
    public String render(final String textContent) {
        LOG.info("[VUE3-VDOM] Rendering VNode tree for content");
        String escaped = textContent
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        String spanNode = String.format(
                "{\"type\":\"span\","
                + "\"props\":{\"class\":\"egds-greeting-text\"},"
                + "\"children\":\"%s\","
                + "\"shapeFlag\":%d}",
                escaped,
                SHAPE_FLAG_TEXT_CHILDREN);
        return String.format(
                "{\"type\":\"div\","
                + "\"props\":{\"id\":\"egds-app\","
                + "\"data-v-app\":\"\"},"
                + "\"children\":[%s],"
                + "\"shapeFlag\":%d,"
                + "\"patchFlag\":%d}",
                spanNode,
                SHAPE_FLAG_ARRAY_CHILDREN,
                PATCH_FLAG_TEXT);
    }
}
