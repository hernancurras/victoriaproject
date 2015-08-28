#include <stdio.h>
#include <cairo.h>
#include <glib.h>

#include "sail_view.h"

static void draw_x_gridline(cairo_t *cr, const int n) {
    int x = n * SAILS_GRID_SPACING;
    cairo_move_to(cr, x + 0.5, -SAILS_GRID_WIDTH);
    cairo_line_to(cr, x + 0.5, SAILS_GRID_WIDTH);
    cairo_stroke(cr);
}

static void draw_y_gridline(cairo_t *cr, const int n) {
    int y = n * SAILS_GRID_SPACING;
    cairo_move_to(cr, -SAILS_GRID_WIDTH, y + 0.5);
    cairo_line_to(cr, SAILS_GRID_WIDTH, y + 0.5);
    cairo_stroke(cr);
}

static void draw_grid(cairo_t *cr) {
    cairo_set_line_width(cr, 1);

    cairo_set_source_rgb(cr, 0.3, 0.3, 0.3);
    int n;
    for (n = -SAILS_GRID_N; n <= SAILS_GRID_N; n++) {
        draw_x_gridline(cr, n);
        draw_y_gridline(cr, n);
    }
}

static void draw_axis(cairo_t *cr) {
    cairo_set_source_rgb(cr, 0.3, 0.3, 0.3);
    cairo_select_font_face(cr, "Sans", CAIRO_FONT_SLANT_NORMAL, CAIRO_FONT_WEIGHT_NORMAL);
    cairo_set_font_size(cr, 11);

    char number_text[SAILS_GRID_NUMBER_STRING_LEN];
    int n;
    for (n = -SAILS_GRID_WIDTH; n <= SAILS_GRID_WIDTH; n += SAILS_GRID_SPACING) {
        cairo_move_to(cr, n + 10, -10);
        snprintf(number_text, SAILS_GRID_NUMBER_STRING_LEN, "%d", n/SAILS_GRID_SPACING);
        cairo_show_text(cr, number_text);
    }
}

void sail_view_draw(cairo_t *cr,
                    int width, int hight,
                    double tx, double ty,
                    double scale) {
    cairo_translate(cr, (width / 2) + tx * scale,
                        (hight / 2) + ty * scale);

    cairo_scale(cr, scale, scale);
    cairo_set_source_rgb(cr, 0.7, 0.7, 1);
    cairo_paint(cr);

    draw_grid(cr);
    draw_axis(cr);
}
