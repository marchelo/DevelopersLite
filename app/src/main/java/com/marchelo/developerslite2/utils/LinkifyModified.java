package com.marchelo.developerslite2.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Patterns;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simper version of {@link Linkify} that works only with URLs. <br/>
 * Difference is that this class method {@link #addLinks(TextView)}
 * do not remove url spans that are already contained in provided textView spannable text.
 *
 * @author Oleg Green
 * @since 22.05.16
 */
public class LinkifyModified {

    /**
     * Scans the text of the provided TextView and turns all occurrences of
     * the link types indicated in the mask into clickable links.  If matches
     * are found the movement method for the TextView is set to
     * LinkMovementMethod.
     */
    public static boolean addLinks(TextView text) {
        CharSequence t = text.getText();

        if (t instanceof Spannable) {
            if (addLinks((Spannable) t)) {
                addLinkMovementMethod(text);
                return true;
            }

            return false;
        } else {
            SpannableString s = SpannableString.valueOf(t);

            if (addLinks(s)) {
                addLinkMovementMethod(text);
                text.setText(s);

                return true;
            }

            return false;
        }
    }

    private static void addLinkMovementMethod(TextView t) {
        MovementMethod m = t.getMovementMethod();

        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (t.getLinksClickable()) {
                t.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    /**
     * Scans the text of the provided Spannable and turns all occurrences
     * of the link types indicated in the mask into clickable links.
     * If the mask is nonzero, it also removes any existing URLSpans
     * attached to the Spannable, to avoid problems if you call it
     * repeatedly on the same text.
     */
    public static boolean addLinks(Spannable text) {
        ArrayList<LinkSpec> links = new ArrayList<>();

        URLSpan[] old = text.getSpans(0, text.length(), URLSpan.class);

        for (int i = old.length - 1; i >= 0; i--) {
            URLSpan urlSpan = old[i];

            LinkSpec linkSpec = new LinkSpec();
            linkSpec.start = text.getSpanStart(urlSpan);
            linkSpec.end = text.getSpanEnd(urlSpan);
            linkSpec.url = urlSpan.getURL();
            links.add(linkSpec);

            text.removeSpan(urlSpan);
        }

        gatherLinks(links, text, Patterns.WEB_URL, new String[]{"http://", "https://", "rtsp://"},
                Linkify.sUrlMatchFilter, null);

        pruneOverlaps(links);

        if (links.size() == 0) {
            return false;
        }

        for (LinkSpec link : links) {
            applyLink(link.url, link.start, link.end, text);
        }

        return true;
    }

    private static void gatherLinks(ArrayList<LinkSpec> links, Spannable s, Pattern pattern, String[] schemes,
                                    Linkify.MatchFilter matchFilter, Linkify.TransformFilter transformFilter) {
        Matcher m = pattern.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            if (matchFilter == null || matchFilter.acceptMatch(s, start, end)) {
                LinkSpec spec = new LinkSpec();

                spec.url = makeUrl(m.group(0), schemes, m, transformFilter);
                spec.start = start;
                spec.end = end;

                links.add(spec);
            }
        }
    }

    private static void pruneOverlaps(ArrayList<LinkSpec> links) {
        Collections.sort(links, (a, b) -> {
            if (a.start < b.start) {
                return -1;
            }

            if (a.start > b.start) {
                return 1;
            }

            if (a.end < b.end) {
                return 1;
            }

            if (a.end > b.end) {
                return -1;
            }

            return 0;
        });

        int len = links.size();
        int i = 0;

        while (i < len - 1) {
            LinkSpec a = links.get(i);
            LinkSpec b = links.get(i + 1);
            int remove = -1;

            if ((a.start <= b.start) && (a.end > b.start)) {
                if (b.end <= a.end) {
                    remove = i + 1;
                } else if ((a.end - a.start) > (b.end - b.start)) {
                    remove = i + 1;
                } else if ((a.end - a.start) < (b.end - b.start)) {
                    remove = i;
                }

                if (remove != -1) {
                    links.remove(remove);
                    len--;
                    continue;
                }
            }
            i++;
        }
    }

    private static void applyLink(String url, int start, int end, Spannable text) {
        URLSpan span = new URLSpan(url);

        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static String makeUrl(String url, String[] prefixes,
                                  Matcher m, Linkify.TransformFilter filter) {
        if (filter != null) {
            url = filter.transformUrl(m, url);
        }

        boolean hasPrefix = false;

        for (String prefix : prefixes) {
            if (url.regionMatches(true, 0, prefix, 0,
                    prefix.length())) {
                hasPrefix = true;

                // Fix capitalization if necessary
                if (!url.regionMatches(false, 0, prefix, 0,
                        prefix.length())) {
                    url = prefix + url.substring(prefix.length());
                }

                break;
            }
        }

        if (!hasPrefix) {
            url = prefixes[0] + url;
        }

        return url;
    }

    static class LinkSpec {
        String url;
        int start;
        int end;
    }
}
