package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class OutlineEditText extends androidx.appcompat.widget.AppCompatEditText {

    private int drawTimes = -1;
    private final Paint idleCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean idleCursorVisible = false;

    public OutlineEditText(Context context) {
        super(context);
    }

    public OutlineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OutlineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {
        if(drawTimes == -1) {
            drawTimes = getTag() == null ? 1 : OutlineTextView.redrawTimes;
        }

        for(int c = 0; c < drawTimes; c++) super.draw(canvas);
        drawIdleCursor(canvas);
    }

    public void setIdleCursorVisible(boolean visible) {
        if (idleCursorVisible == visible) {
            return;
        }
        idleCursorVisible = visible;
        invalidate();
    }

    private void drawIdleCursor(Canvas canvas) {
        if (!idleCursorVisible || isCursorVisible()) {
            return;
        }

        CharSequence text = getText();
        int end = text == null ? 0 : text.length();
        float x = getCompoundPaddingLeft() - getScrollX();
        if (end > 0) {
            x += getPaint().measureText(text, 0, end);
        }

        int top = getExtendedPaddingTop();
        int bottom = getHeight() - getExtendedPaddingBottom();
        if (bottom <= top) {
            top = getPaddingTop();
            bottom = getHeight() - getPaddingBottom();
        }

        idleCursorPaint.setColor(getCurrentTextColor());
        idleCursorPaint.setStrokeWidth(Math.max(2f, getResources().getDisplayMetrics().density * 2f));
        canvas.drawLine(x, top, x, bottom, idleCursorPaint);
    }
}
