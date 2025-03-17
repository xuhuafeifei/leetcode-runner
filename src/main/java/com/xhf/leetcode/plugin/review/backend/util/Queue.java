package com.xhf.leetcode.plugin.review.backend.util;

/**
 * @author 文艺倾年
 */
public class Queue<ContentType> {

    private class QueueNode {

        private ContentType content = null;
        private QueueNode nextNode = null;

        /**
         * 创建一个新的 QueueNode<ContentType> 对象。
         * 内容通过参数设置，初始时下一个节点为空。
         *
         * @param pContent 节点的内容，类型为 ContentType
         */
        public QueueNode(ContentType pContent) {
            content = pContent;
            nextNode = null;
        }

        /**
         * 设置下一个节点的引用。
         *
         * @param pNext 下一个节点
         */
        public void setNext(QueueNode pNext) {
            nextNode = pNext;
        }

        /**
         * 返回当前节点的下一个节点。
         *
         * @return 下一个节点，类型为 QueueNode
         */
        public QueueNode getNext() {
            return nextNode;
        }

        /**
         * 返回当前节点的内容。
         *
         * @return 节点的内容，类型为 ContentType
         */
        public ContentType getContent() {
            return content;
        }

    }

    private QueueNode head;
    private QueueNode tail;

    /**
     * 创建一个空队列。
     * 队列中管理的对象必须是 ContentType 类型。
     */
    public Queue() {
        head = null;
        tail = null;
    }

    /**
     * 如果队列为空，返回 true，否则返回 false。
     *
     * @return 如果队列为空，返回 true；否则返回 false
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * 将对象 pContent 添加到队列的末尾。
     * 如果 pContent 为 null，则队列不变。
     *
     * @param pContent 要添加的对象，类型为 ContentType
     */
    public void enqueue(ContentType pContent) {
        if (pContent != null) {
            QueueNode newNode = new QueueNode(pContent);
            if (this.isEmpty()) {
                head = newNode;
                tail = newNode;
            } else {
                tail.setNext(newNode);
                tail = newNode;
            }
        }
    }

    /**
     * 移除队列的第一个对象。
     * 如果队列为空，则队列不变。
     */
    public void dequeue() {
        if (!this.isEmpty()) {
            head = head.getNext();
            if (this.isEmpty()) {
                head = null;
                tail = null;
            }
        }
    }

    /**
     * 返回队列的第一个对象，但不移除它。
     * 如果队列为空，则返回 null。
     *
     * @return 队列的第一个对象，类型为 ContentType；如果队列为空，返回 null
     */
    public ContentType front() {
        if (this.isEmpty()) {
            return null;
        } else {
            return head.getContent();
        }
    }
}
