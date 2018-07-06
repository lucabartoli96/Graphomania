package g.frith.graphomania

import android.graphics.*


const val BORDER_TEXT = 5f

private val arcPath = Path()
private val arrowPath = Path()
private val rect = RectF()
private val bounds = Rect()
private val textBackPaint = Paint()
private val textBackRect = RectF()


fun Canvas.drawArrowHead(verX: Float, verY: Float,
                         lineAngle: Float,  angle: Float,
                         radius: Float, paint: Paint) {

    arrowPath.rewind()
    arrowPath.fillType = Path.FillType.EVEN_ODD
    arrowPath.moveTo(verX, verY)

    val t1 = lineAngle - angle / 2.0
    val t2 = lineAngle + angle / 2.0

    arrowPath.lineTo((verX - radius * Math.cos(t1).toFloat()), (verY - radius * Math.sin(t1).toFloat()))
    arrowPath.lineTo((verX - radius * Math.cos(t2).toFloat()), (verY - radius * Math.sin(t2).toFloat()))
    arrowPath.close()

    this.drawPath(arrowPath, paint)
}


fun Canvas.drawLabel(middleX: Float, middleY: Float, text: String, textPaint: Paint) {


    textPaint.getTextBounds(text, 0, text.length, bounds)

    val textWidth = bounds.width().toFloat()
    val textHeight = bounds.height().toFloat()

    val textX = middleX - textWidth/2
    val textY = middleY + textHeight/2

    textBackPaint.style = Paint.Style.FILL
    textBackPaint.color = Color.WHITE

    textBackRect.set(textX, textY, textWidth, textHeight)

    drawRect(textX,
            textY - textHeight,
            textX + textWidth + BORDER_TEXT,
            textY + BORDER_TEXT,
            textBackPaint)

    drawText(text, textX, textY, textPaint)
}

//private val dotPaint = Paint()
//
//
//fun Canvas.drawDot(p: PointF) {
//    val size = 2
//    this.drawRect(p.x - size, p.y -size, p.x + size, p.y + size, dotPaint)
//}


private fun Canvas.drawStraightEdgeMain(fromX: Float, fromY: Float, //from
                                        toX: Float, toY: Float, //to
                                        paint: Paint, offset: Float, //other params
                                        arrowRadius: Float? = null, arrowAngle: Float? = null,
                                        arrowPaint: Paint? = null) {

    val start = getPointOnSegment(fromX, fromY, toX, toY, offset)
    val end = getPointOnSegment(toX, toY, fromX, fromY, offset)

    this.drawLine(start.x, start.y, end.x, end.y, paint)

    if ( arrowRadius !== null && arrowAngle !== null && arrowPaint !== null ) {
        drawArrowHead(
                end.x, end.y,
                getInclination(start.x, start.y, end.x, end.y),
                Math.toRadians(arrowAngle.toDouble()).toFloat(),
                arrowRadius, arrowPaint
        )
    }

}


private fun Canvas.drawCurveEdgeMain(fromX: Float, fromY: Float,
                                     toX: Float, toY: Float,
                                     curve: Float, offset: Float, paint: Paint,
                                     arrowRadius: Float? = null, arrowAngle: Float? = null,
                                     arrowPaint: Paint? = null,
                                     text: String? = null, textPaint: Paint? = null) {

    if ( curve == 0f ) {
        this.drawStraightEdgeMain(fromX, fromY, toX, toY,
                paint, offset, arrowRadius,
                arrowAngle, arrowPaint)
        return
    }

    val radius = findRadius(fromX, fromY, toX, toY, curve)
    val offsetAngle = getCenterAngle(radius, offset)
    val center = getCenter(fromX, fromY, toX, toY, radius)

    val r = Math.abs(radius)

    val circleStartX = center.x+r
    val circleStartY = center.y

    val startAngle = getAngleDegrees(
            center.x, center.y, fromX, fromY,
            circleStartX, circleStartY) + offsetAngle

    val toAngle = getAngleDegrees(
            center.x, center.y, toX, toY,
            circleStartX, circleStartY) - startAngle - offsetAngle

    val fixSign = if ( radius * toAngle < 0 ) {
        Math.signum(radius) * 360f
    }  else {
        0f
    }

    val sweepAngle = toAngle + fixSign

    rect.set(center.x - r, center.y - r,
            center.x + r, center.y + r)

    arcPath.rewind()
    arcPath.addArc(rect, startAngle, sweepAngle)
    this.drawPath(arcPath, paint)


    if ( arrowRadius !== null && arrowAngle !== null && arrowPaint !== null ) {
        val endX = getRotatedX(center.x, center.y, toX, toY, -offsetAngle)
        val endY = getRotatedY(center.x, center.y, toX, toY, -offsetAngle)

        val lineAngle = getPerpendicular(center.x, center.y, endX, endY) + if(curve > 0)
            Math.toRadians(180.0).toFloat()
        else
            0f

        this.drawArrowHead( endX, endY, lineAngle,
                Math.toRadians(arrowAngle.toDouble()).toFloat(),
                arrowRadius, arrowPaint)
    }


    if ( text !== null && textPaint !== null ) {
        val halfAngle = getAngleDegrees(center.x, center.y, fromX, fromY, toX, toY)/2
        val rotAngle = halfAngle + if ( Math.abs(halfAngle) > 90 ) 180f else 0f

        val middleX = getRotatedX(center.x, center.y, toX, toY, rotAngle)
        val middleY = getRotatedY(center.x, center.y, toX, toY, rotAngle)

        drawLabel(middleX, middleY, text, textPaint)
    }


}


private fun Canvas.drawLoopMain(fromX: Float, fromY: Float, radius: Float, angle: Float,
                                offset: Float, paint: Paint,
                                arrowRadius: Float? = null, arrowAngle: Float? = null,
                                arrowPaint: Paint? = null,
                                text: String? = null, textPaint: Paint? = null) {

    val centerX = getRotatedX(fromX, fromY, fromX + radius, fromY, angle)
    val centerY = getRotatedY(fromX, fromY, fromX + radius, fromY, angle)

    val offsetAngle = getCenterAngle(radius, offset)

    val pX = getRotatedX(centerX, centerY, fromX, fromY, -offsetAngle)
    val pY = getRotatedY(centerX, centerY, fromX, fromY, -offsetAngle)
    val startAngle = getAngleDegrees(centerX, centerY, pX, pY, centerX + radius, centerY)

    rect.set(centerX - radius, centerY - radius,
            centerX + radius, centerY + radius)

    arcPath.rewind()
    arcPath.addArc(rect, startAngle + 2*offsetAngle, 360f - 2*offsetAngle)
    this.drawPath(arcPath, paint)

    if ( arrowRadius !== null && arrowAngle !== null && arrowPaint !== null ) {
        val lineAngle = getInclination(fromX, fromY, pX, pY)
        val rightAngle =  lineAngle - Math.toRadians(215.0).toFloat()

        drawArrowHead(pX, pY, rightAngle,
                      Math.toRadians(arrowAngle.toDouble()).toFloat(),
                      arrowRadius, arrowPaint)
    }

    if ( text !== null  && textPaint !== null ) {
        val middleX = getRotatedX(fromX, fromY, fromX + 2 * radius, fromY, angle)
        val middleY = getRotatedY(fromX, fromY, fromX + 2 * radius, fromY, angle)

        drawLabel(middleX, middleY, text, textPaint)
    }

}





private fun Canvas.drawStraightEdge(fromX: Float, fromY: Float, //from
                                    toX: Float, toY: Float, //to
                                    paint: Paint, offset: Float) { //other params
    drawStraightEdgeMain(fromX, fromY, toX, toY, paint, offset)
}


private fun Canvas.drawArrowedStraightEdge(fromX: Float, fromY: Float, //from
                                           toX: Float, toY: Float, //to
                                           paint: Paint, offset: Float, //other params
                                           arrowRadius: Float, arrowAngle: Float,
                                           arrowPaint: Paint) { //arrow params

    drawStraightEdgeMain(fromX, fromY, toX, toY, paint, offset,
            arrowRadius, arrowAngle, arrowPaint)
}




fun Canvas.drawCurveEdge(fromX: Float, fromY: Float, //from
                         toX: Float, toY: Float,//to
                         curve: Float, offset: Float, paint: Paint) { //other params

    drawCurveEdgeMain(
            fromX, fromY, toX, toY, curve, offset, paint
    )
}

fun Canvas.drawLabeledCurveEdge(fromX: Float, fromY: Float, //from
                                toX: Float, toY: Float,//to
                                curve: Float, offset: Float, paint: Paint,
                                text: String, textPaint: Paint //label params
                                ) {

    drawCurveEdgeMain(
            fromX, fromY, toX, toY, curve, offset, paint,
            text = text, textPaint = textPaint
    )

}

fun Canvas.drawCurveArrowedEdge(fromX: Float, fromY: Float, //from
                                toX: Float, toY: Float,//to
                                curve: Float, offset: Float, paint: Paint,
                                arrowRadius: Float, arrowAngle: Float,
                                arrowPaint: Paint) { //arrow params
    drawCurveEdgeMain(
            fromX, fromY, toX, toY, curve, offset, paint,
            arrowRadius, arrowAngle, arrowPaint
    )
}

fun Canvas.drawArrowedCurveLabeledEdge(fromX: Float, fromY: Float, //from
                                toX: Float, toY: Float,//to
                                curve: Float, offset: Float, paint: Paint, //other params
                                arrowRadius: Float, arrowAngle:
                                Float, arrowPaint: Paint, //arrow params
                                text: String, textPaint: Paint) {  //label params
    drawCurveEdgeMain(
            fromX, fromY, toX, toY, curve, offset, paint,
            arrowRadius, arrowAngle, arrowPaint,
            text, textPaint
    )
}

//TODO: finire interfaccia Loop

fun Canvas.drawArrowedLabeledLoop(fromX: Float, fromY: Float, radius: Float, angle: Float,
                                  offset: Float, paint: Paint,
                                  arrowRadius: Float, arrowAngle: Float,
                                  arrowPaint: Paint,
                                  text: String, textPaint: Paint) {

    drawLoopMain(fromX, fromY, radius, angle, offset, paint,
                 arrowRadius, arrowAngle, arrowPaint,
                 text, textPaint)
}