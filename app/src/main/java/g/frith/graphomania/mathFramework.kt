package g.frith.graphomania

import android.graphics.PointF
import java.util.*


fun Random.nextAngleDegrees(): Float {
    return this.nextFloat()*360f
}


/**
 * Let c = (cX, cY), p1 = (x1, y1) and p2 = (x2, y2), returns angle p1-c^-p2
 */
fun getAngle(cX: Float, cY: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val angle1 = Math.atan2(
            (y1- cY).toDouble(),
            (x1 - cX).toDouble()
    )
    val angle2 = Math.atan2(
            (y2 - cY).toDouble(),
            (x2 - cX).toDouble()
    )

    return (angle1 - angle2).toFloat()
}

fun getAngleDegrees(cX: Float, cY: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return Math.toDegrees(getAngle(cX, cY, x1, y1, x2, y2).toDouble()).toFloat()
}


/**
 *  Let p1 = (x1, y1), p2 = (x2, y2) and p3 = (x3, y3), returns area of triangle p1-p2-p3
 */
fun getTriangleArea(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
    return Math.abs((x1 - x3)*(y2 - y1) - (x1 - x2)*(y3 - y1))
}

/**
 *  Let p1 = (x1, y1), p2 = (x2, y2) returns the distance between p1-p2
 */
fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return Math.hypot((x1 - x2).toDouble(), (y1 - y2).toDouble()).toFloat()
}

/**
 *  Let p1 = (x1, y1), p2 = (x2, y2) and p3 = (x3, y3), returns distance between p1 and the
 *  line passing from p2-p3
 */
fun getDistanceToLine(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Float {
    return getTriangleArea(x1, y1, x2, y2, x3, y3) / getDistance(x2, y2, x3, y3)
}


/**
 *  Let p1 = (x1, y1), p2 = (x2, y2) and p3 = (x3, y3), returns true if p1 is above the
 *  line passing from p2-p3
 */
fun isAboveLine(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float): Boolean {
    return (y1 - y2)/(y3 - y2) > (x1 - x2)/(x3 - x2)
}


/**
 *  Let p1 = (x1, y1) and p2 = (x2, y2) and p3 = (x3, y3), returns the slope
 *  the line passing from p1-p2
 */
fun getSlope(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return (y2 - y1)/(x2 - x1)
}

/**
 *  Let p1 = (x1, y1) and p2 = (x2, y2) and p3 = (x3, y3), returns the inclination
 *  the line passing from p1-p2
 */
fun getInclination(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return Math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()).toFloat()
}


/**
 *  Let p1 = (x1, y1) and p2 = (x2, y2) and p3 = (x3, y3), returns the inclination
 *  the perpendicular of the one passing from p1-p2
 */
fun getPerpendicular(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return Math.atan2(-(x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()
}


/**
 *  Let p1 = (x1, y1) and p2 = (x2, y2) and angle be in degrees, returns the x value
 *  of the p2 rotated clockwise around p1 of angle
 */
fun getRotatedX(x1: Float, y1: Float, x2: Float, y2: Float, angle: Float): Float {
    val tX = x2 - x1
    val tY = y2 - y1

    val angleRad = Math.toRadians(angle.toDouble())

    return (x1 + Math.cos(angleRad)*tX - Math.sin(angleRad)*tY).toFloat()
}


/**
 *  Let p1 = (x1, y1) and p2 = (x2, y2) and angle be in degrees, returns the y value
 *  of the p2 rotated clockwise around p1 of angle
 */
fun getRotatedY(x1: Float, y1: Float, x2: Float, y2: Float, angle: Float): Float {
    val tX = x2 - x1
    val tY = y2 - y1

    val angleRad = Math.toRadians(angle.toDouble())

    return (y1 + Math.sin(angleRad)*tX + Math.cos(angleRad)*tY).toFloat()
}

/**
 * Like getDistanceToLine but with returns, conventionally, the positive value
 * if a is above and negative if it is under the line
 */
fun getDistanceToLineSigned(aX: Float, aY: Float,
                            bX: Float, bY: Float,
                            cX: Float, cY: Float): Float {

    val sign = ( if ( (bX < cX) == (bY > cY) ) 1 else -1 ) *
            ( if( isAboveLine(aX, aY, bX, bY, cX, cY) ) 1 else -1 )

    return sign * getDistanceToLine(aX, aY, bX, bY, cX, cY)

}


/**
 * Let p1 = (x1, y1) and p2 = (x2, y2), returns the center of a possible circle
 * that passes from p1, p2 with given radius.
 * There are two possible centers, returns the left one if the radius is
 * conventionally, negative.
 */
fun getCenter(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float): PointF {

    val r = Math.abs(radius)
    val sign = Math.signum(radius)

    val distance = Math.hypot((x1 - x2).toDouble(), (y1 - y2).toDouble())
    val diameter = 2.0 * r.toDouble()

    val center = PointF((x1 + x2) / 2f , (y1 + y2) / 2f)

    if (distance == diameter)
        return center

    val mirrorDistance = Math.sqrt(r * r - distance * distance / 4.0)
    val dx = ((x2 - x1) * mirrorDistance / distance).toFloat()
    val dy = ((y2 - y1) * mirrorDistance / distance).toFloat()

    return PointF(center.x - sign*dy, center.y + sign*dx)
}


/**
 * Returns the radius of a circle given the width of the curve with the segment specified
 */
fun findRadius(fromX: Float, fromY: Float, toX: Float, toY: Float, curve: Float): Float {
    val d = getDistance(fromX, fromY, toX, toY)
    return (curve + d*d/(4*curve))/2f
}


/**
 * returns the angle on the center given the radius and an offset on the circle
 */
fun getCenterAngle(radius: Float, offset: Float): Float {

    return if (offset == 0f ){
        0f
    } else {
        2*Math.toDegrees(Math.asin(offset/(2*radius).toDouble())).toFloat()
    }

}


/**
 *  Returns a point on the segment with offset distance from (fromX, fromY)
 */
fun getPointOnSegment(fromX: Float, fromY: Float, toX: Float, toY: Float, offset: Float): PointF {

    val b = Math.abs(fromX - toX)
    val c = Math.abs(fromY - toY)
    val a = Math.sqrt((b*b + c*c).toDouble())

    val angle = Math.asin(b/a)

    val x = offset * Math.sin(angle).toFloat()
    val y = offset * Math.cos(angle).toFloat()

    return PointF(
            fromX + Math.signum(toX - fromX) * x,
            fromY - Math.signum(fromY - toY) * y
    )
}