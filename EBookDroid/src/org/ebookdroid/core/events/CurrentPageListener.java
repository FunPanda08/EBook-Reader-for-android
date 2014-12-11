package org.ebookdroid.core.events;

import org.ebookdroid.core.PageIndex;

public interface CurrentPageListener {

    void currentPageChanged(PageIndex oldIndex, PageIndex newIndex);
}
