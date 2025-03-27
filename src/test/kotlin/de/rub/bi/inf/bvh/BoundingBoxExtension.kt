package de.rub.bi.inf.bvh.de.rub.bi.inf.bvh

import javax.media.j3d.BoundingBox
import javax.vecmath.Point3d

fun BoundingBox.lower() = Point3d().apply { this@lower.getLower(this) }
fun BoundingBox.upper() = Point3d().apply { this@upper.getUpper(this) }

fun BoundingBox.contains(point: Point3d): Boolean {
    val min = this.lower()
    val max = this.upper()

    return (point.x >= min.x && point.x <= max.x) &&
            (point.y >= min.y && point.y <= max.y) &&
            (point.z >= min.z && point.z <= max.z)
}