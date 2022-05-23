package com.simple.pagination.util;

import com.simple.pagination.exception.UnsupportedReturnTypeException;

import java.util.*;

/**
 * @author chengxing.wu@xiaobao100.com
 * @date 2021/6/21 15:12
 */
public class PaginationCollectionFactory {
    private PaginationCollectionFactory() {
    }

    public static Collection<?> build(Class<?> returnType, Object result, Long total, Integer page, Integer size) {
        if (result == null) {
            return build(returnType, total, page, size);
        } else {
            Collection<?> collection = (Collection) result;
            if (collection.isEmpty()) {
                return build(returnType, total, page, size);
            } else {
                if (List.class.isAssignableFrom(returnType)) {
                    if (List.class == returnType || ArrayList.class == returnType) {
                        return new PaginationArrayList(collection, total, page, size);
                    }

                    if (LinkedList.class == returnType) {
                        return new PaginationLinkedList(collection, total, page, size);
                    }
                } else if (Set.class.isAssignableFrom(returnType)) {
                    if (Set.class == returnType || HashSet.class == returnType) {
                        return new PaginationHashSet(collection, total, page, size);
                    }

                    if (LinkedHashSet.class == returnType) {
                        return new PaginationLinkedHashSet(collection, total, page, size);
                    }

                    if (TreeSet.class == returnType) {
                        return new PaginationTreeSet(collection, total, page, size);
                    }
                } else if (Queue.class.isAssignableFrom(returnType)) {
                    if (Queue.class == returnType || ArrayDeque.class == returnType) {
                        return new PaginationArrayDeque(collection, total, page, size);
                    }

                    if (PriorityQueue.class == returnType) {
                        return new PaginationPriorityQueue(collection, total, page, size);
                    }
                }

                throw new UnsupportedReturnTypeException("The return type \"" + returnType.getName() + "\" is not supported, please use Java Collection.");
            }
        }
    }

    /**
     * List.class.isAssignableFrom(returnType): 校验是否是List的子类
     *
     * @param returnType
     * @param total
     * @param page
     * @param size
     * @return
     */
    public static Collection<?> build(Class<?> returnType, Long total, Integer page, Integer size) {
        if (List.class.isAssignableFrom(returnType)) {
            if (List.class == returnType || ArrayList.class == returnType) {
                return new PaginationArrayList(total, page, size);
            }

            if (LinkedList.class == returnType) {
                return new PaginationLinkedList(total, page, size);
            }
        } else if (Set.class.isAssignableFrom(returnType)) {
            if (Set.class == returnType || HashSet.class == returnType) {
                return new PaginationHashSet(total, page, size);
            }

            if (LinkedHashSet.class == returnType) {
                return new PaginationLinkedHashSet(total, page, size);
            }

            if (TreeSet.class == returnType) {
                return new PaginationTreeSet(total, page, size);
            }
        } else if (Queue.class.isAssignableFrom(returnType)) {
            if (Queue.class == returnType || ArrayDeque.class == returnType) {
                return new PaginationArrayDeque(total, page, size);
            }

            if (PriorityQueue.class == returnType) {
                return new PaginationPriorityQueue(total, page, size);
            }
        }

        throw new UnsupportedReturnTypeException("The return type \"" + returnType.getName() + "\" is not supported, please use Java Collection.");
    }

    public static Collection<?> empty(Class<?> returnType) {
        return build(returnType, 0L, 0, 0);
    }
}
