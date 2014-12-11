package org.ebookdroid.core;

import org.ebookdroid.common.settings.types.DocumentViewMode;

import android.graphics.RectF;

import java.util.Queue;

public abstract class AbstractEventScroll<E extends AbstractEventScroll<E>> extends AbstractEvent {

    private final Queue<E> eventQueue;

    protected PageTreeLevel level;

    protected AbstractEventScroll(final Queue<E> eventQueue) {
        this.eventQueue = eventQueue;
    }

    final void init(final AbstractViewController ctrl) {
        this.viewState = ViewState.get(ctrl);
        this.ctrl = ctrl;
        this.level = PageTreeLevel.getLevel(viewState.zoom);
    }

    @SuppressWarnings("unchecked")
    final void release() {
        this.ctrl = null;
        this.viewState = null;
        this.level = null;
        this.bitmapsToRecycle.clear();
        this.nodesToDecode.clear();
        eventQueue.offer((E) this);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.AbstractEvent#process()
     */
    @Override
    public final ViewState process() {
        try {
            super.process();
            final Page page = viewState.pages.getCurrentPage();
            if (page != null) {
                if (ctrl.mode != DocumentViewMode.SINGLE_PAGE) {
                    ctrl.model.setCurrentPageIndex(page.index);
                }
                ctrl.updatePosition(page, viewState);
            }
            ctrl.getView().redrawView(viewState);
            return viewState;
        } finally {
            release();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.IEvent#process(org.ebookdroid.core.ViewState, org.ebookdroid.core.PageTree)
     */
    @Override
    public boolean process(final PageTree nodes) {
        if (level.next != null) {
            nodes.recycleNodes(level.next, bitmapsToRecycle);
        }
        return process(nodes, level);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ebookdroid.core.IEvent#process(org.ebookdroid.core.ViewState, org.ebookdroid.core.PageTreeNode)
     */
    @Override
    public boolean process(final PageTreeNode node) {

        final RectF pageBounds = viewState.getBounds(node.page);

        if (!viewState.isNodeKeptInMemory(node, pageBounds)) {
            node.recycle(bitmapsToRecycle);
            return false;
        }

        if (!node.holder.hasBitmaps()) {
            node.decodePageTreeNode(nodesToDecode, viewState);
        }

        return true;
    }
}
