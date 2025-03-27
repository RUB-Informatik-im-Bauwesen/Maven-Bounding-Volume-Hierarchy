package de.rub.bi.inf.bvh

import de.rub.bi.inf.bvh.de.rub.bi.inf.bvh.contains
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import java.util.*
import javax.media.j3d.BoundingBox
import javax.vecmath.Point3d
import kotlin.test.Test
import kotlin.test.assertContentEquals


class BVHTreeTest {

    @Test
    fun testBVHTreeConstruction() {
        val data = mapOf(
            "A" to BoundingBox(Point3d(0.0, 0.0, 0.0), Point3d(1.0, 1.0, 1.0)),
            "B" to BoundingBox(Point3d(2.0, 2.0, 2.0), Point3d(3.0, 3.0, 3.0)),
            "C" to BoundingBox(Point3d(4.0, 4.0, 4.0), Point3d(5.0, 5.0, 5.0))
        )
        val bvhTree = BVHTree(data)
        assertNotNull(bvhTree)
    }

    @Test
    fun testBVHTreeTraversal() {
        val data = mapOf(
            "A" to BoundingBox(Point3d(0.0, 0.0, 0.0), Point3d(1.0, 1.0, 1.0)),
            "B" to BoundingBox(Point3d(2.0, 2.0, 2.0), Point3d(3.0, 3.0, 3.0))
        )
        val bvhTree = BVHTree(data)
        val visitedObjects = mutableListOf<String>()

        bvhTree.traverse { _, obj: Optional<String> ->
            obj.ifPresent { visitedObjects.add(it) }
            true
        }

        assertEquals(setOf("A", "B"), visitedObjects.toSet())
    }

    @Test
    fun testBVHOrderTraversal() {
        val data = mapOf(
            "A" to BoundingBox(Point3d(0.0, 0.0, 0.0), Point3d(1.0, 1.0, 1.0)),
            "B" to BoundingBox(Point3d(-2.0, -2.0, -2.0), Point3d(-3.0, -3.0, -3.0)),
            "C" to BoundingBox(Point3d(4.0, 4.0, 4.0), Point3d(5.0, 5.0, 5.0)),
        )
        val bvhTree = BVHTree(data)
        val visitedObjects = mutableListOf<String>()

        bvhTree.traverse { b, obj: Optional<String> ->
            if (obj.isPresent) visitedObjects.add(obj.get())
            true
        }

        println(visitedObjects.toTypedArray().contentToString())
        assertContentEquals(arrayOf("B", "A", "C"), visitedObjects.toTypedArray())
    }

    @Test
    fun testBVHFindTraversal() {
        val data = mapOf(
            "A" to BoundingBox(Point3d(0.0, 0.0, 0.0), Point3d(1.0, 1.0, 1.0)),
            "B" to BoundingBox(Point3d(2.0, 2.0, 2.0), Point3d(3.0, 3.0, 3.0)),
            "C" to BoundingBox(Point3d(4.0, 4.0, 4.0), Point3d(5.0, 5.0, 5.0)),
        )
        val bvhTree = BVHTree(data)
        val visitedObjects = mutableListOf<String>()

        val pointToFind = Point3d(0.5, 0.5, 0.5)

        bvhTree.traverse { b, obj: Optional<String> ->
            if (!b.contains(pointToFind)) return@traverse false
            if (obj.isPresent) visitedObjects.add(obj.get())
            return@traverse true
        }

        assertEquals(setOf("A"), visitedObjects.toSet())
    }

    @Test
    fun testLongestAxisCalculation() {
        val boundingBox = BoundingBox(Point3d(0.0, 0.0, 0.0), Point3d(4.0, 2.0, 1.0))
        val bvhTree = BVHTree(emptyMap<String, BoundingBox>())
        val longestAxis = bvhTree.javaClass.getDeclaredMethod("getLongestAxis", BoundingBox::class.java)
        longestAxis.isAccessible = true
        val result = longestAxis.invoke(bvhTree, boundingBox) as Int
        assertEquals(0, result) // X-axis should be the longest
    }
}
