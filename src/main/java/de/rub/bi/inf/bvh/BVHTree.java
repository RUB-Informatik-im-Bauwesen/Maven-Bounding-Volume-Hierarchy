package de.rub.bi.inf.bvh;

import javax.media.j3d.BoundingBox;
import javax.vecmath.Point3d;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BVHTree<T> {
    private final BVHNode root;

    public BVHTree(Map<T, BoundingBox> data) {
        root = buildTree(data);
    }

    private BVHNode buildTree(Map<T, BoundingBox> data) {
        if (data.isEmpty()) {
            return null;
        } else if (data.size() == 1) {
            // Create a leaf node for a single object
            final var entry = data.entrySet().iterator().next();
            return new BVHLeaf<>(entry.getValue(), entry.getKey());
        }

        final var combinedBoundingBox = data.values().stream().reduce(data.values().iterator().next(), (bigBox, element) -> {
            bigBox.combine(element);
            return bigBox;
        });
        final var longestAxis = getLongestAxis(combinedBoundingBox);
        final var sortedObjects = data.entrySet().stream().sorted(Comparator.comparingDouble(box -> {
            final var center = getBoundingBoxCenter(box.getValue());
            final double[] coords = {center.x, center.y, center.z};
            return coords[longestAxis];
        })).toList();

        final var mid = sortedObjects.size() / 2;
        final var leftObjects = sortedObjects.subList(0, mid);
        final var rightObjects = sortedObjects.subList(mid, sortedObjects.size());

        return new InternalNode(combinedBoundingBox, buildTree(leftObjects.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))), buildTree(rightObjects.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

    private void traverseTree(Iterator<T> iterator, BVHNode node) {
        if (node instanceof InternalNode internalNode && iterator.iterationStep(node.boundingBox, Optional.empty())) {
            traverseTree(iterator, internalNode.left);
            traverseTree(iterator, internalNode.right);
            return;
        }
        if (node instanceof BVHLeaf<?> leaf) {
            iterator.iterationStep(node.boundingBox, Optional.of(((T) leaf.data)));
        }
    }

    public void traverse(Iterator<T> iterator) {
        this.traverseTree(iterator, root);
    }

    private int getLongestAxis(BoundingBox boundingBox) {
        final Point3d min = new Point3d(), max = new Point3d();
        boundingBox.getLower(min);
        boundingBox.getUpper(max);

        final var lengths = List.of(max.x - min.x, max.y - min.y, max.z - min.z);
        return lengths.indexOf(lengths.stream().mapToDouble(Double::doubleValue).max().getAsDouble());
    }

    private Point3d getBoundingBoxCenter(BoundingBox boundingBox) {
        final Point3d min = new Point3d(), max = new Point3d();
        boundingBox.getLower(min);
        boundingBox.getUpper(max);

        return new Point3d((min.x + max.x) / 2.0, (min.y + max.y) / 2.0, (min.z + max.z) / 2.0);
    }

    public interface Iterator<J> {
        boolean iterationStep(BoundingBox b, Optional<J> object);
    }

    private abstract static class BVHNode {
        public final BoundingBox boundingBox;

        BVHNode(BoundingBox boundingBox) {
            this.boundingBox = boundingBox;
        }
    }

    private static class InternalNode extends BVHNode {
        public final BVHNode left, right;

        InternalNode(BoundingBox boundingBox, BVHNode left, BVHNode right) {
            super(boundingBox);
            this.left = left;
            this.right = right;
        }
    }

    private static class BVHLeaf<J> extends BVHNode {
        public final J data;

        BVHLeaf(BoundingBox boundingBox, J data) {
            super(boundingBox);
            this.data = data;
        }
    }
}
