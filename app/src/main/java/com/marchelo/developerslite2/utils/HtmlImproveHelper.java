package com.marchelo.developerslite2.utils;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

/**
 * @author Oleg Green
 * @since 22.05.16
 */
public class HtmlImproveHelper {

    /**
     * Replaces all image spans with plain text(i.e. source text of image span) <br/>
     * This method should be used after {@link android.text.Html#fromHtml(String)}
     * or {@link android.text.Html#fromHtml(String, Html.ImageGetter, Html.TagHandler)}
     *
     * @param spanned where to search for image spans that will be replaces with text
     * @return modified {@link Spanned} with text instead of image spans.
     */
    public static Spanned replaceImageSpansWithPlainText(Spanned spanned) {
        SpannableStringBuilder builder = new SpannableStringBuilder(spanned);
        ImageSpan[] imageSpans = builder.getSpans(0, builder.length(), ImageSpan.class);
        for (ImageSpan imageSpan : imageSpans) {
            int spanStart = builder.getSpanStart(imageSpan);
            int spanEnd = builder.getSpanEnd(imageSpan);

            builder.replace(spanStart, spanEnd, imageSpan.getSource());

            builder.removeSpan(imageSpan);
        }
        return builder;
    }
}
