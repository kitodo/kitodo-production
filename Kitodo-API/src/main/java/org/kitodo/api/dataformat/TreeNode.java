package org.kitodo.api.dataformat;

import java.util.List;

public interface TreeNode<T> {
    public List<T> getChildren();
}
