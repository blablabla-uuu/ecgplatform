import os
cur_dir = os.path.dirname(os.path.abspath(__file__))

from django.contrib.auth.models import User

from reportlab.lib.pagesizes import A4
from reportlab.lib.units import cm
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_RIGHT
from reportlab.lib.colors import navy, yellow, red
from geraldo import Report, ReportBand, Label, ObjectValue, SystemField
from geraldo import FIELD_ACTION_COUNT, FIELD_ACTION_AVG, FIELD_ACTION_MIN
from geraldo import FIELD_ACTION_MAX, FIELD_ACTION_SUM, FIELD_ACTION_DISTINCT_COUNT, BAND_WIDTH
from geraldo import RoundRect, Line, Image

class UsersReport(Report):
    title = 'Using chart from CairoPlot'
    
    class band_summary(ReportBand):
        height = 5*cm
        elements = [
            Label(text="Users count:", top=0.1*cm, left=0.2*cm),
            ObjectValue(attribute_name='username', top=0.1*cm, left=4*cm, action=FIELD_ACTION_COUNT, display_format='%s permissions found'),
            
            Label(text="Users ids average:", top=0.6*cm, left=0.2*cm),
            ObjectValue(attribute_name='id', top=0.6*cm, left=4*cm, action=FIELD_ACTION_AVG),
 
            Label(text="Users ids minimum:", top=1.1*cm, left=0.2*cm),
            ObjectValue(attribute_name='id', top=1.1*cm, left=4*cm, action=FIELD_ACTION_MIN),
 
            Label(text="Users ids maximum:", top=1.6*cm, left=0.2*cm),
            ObjectValue(attribute_name='id', top=1.6*cm, left=4*cm, action=FIELD_ACTION_MAX),

            Label(text="Users ids sum:", top=2.1*cm, left=0.2*cm),
            ObjectValue(attribute_name='id', top=2.1*cm, left=4*cm, action=FIELD_ACTION_SUM),

            Label(text="Users first name distinct:", top=2.6*cm, left=0.2*cm),
            ObjectValue(attribute_name='first_name', top=2.6*cm, left=4*cm, action=FIELD_ACTION_DISTINCT_COUNT),

            Image(filename=os.path.join(cur_dir, 'output/matplotlib.png'), left=11*cm, top=0.2*cm)
        ]
        borders = {'top': True}
    
    class band_page_header(ReportBand):
        height = 1.3*cm
        elements = [
            SystemField(expression='%(report_title)s', top=0.1*cm, left=0, width=BAND_WIDTH, style={'fontName': 'Helvetica-Bold', 'fontSize': 14, 'alignment': TA_CENTER, 'textColor': navy}),
            Label(text="ID", top=0.8*cm, left=0),
            Label(text="Username", top=0.8*cm, left=3*cm),
            Label(text="First name", top=0.8*cm, left=8*cm),
            Label(text="Last name", top=0.8*cm, left=13*cm),
            Label(text="Staff", top=0.8*cm, left=18*cm),                    
        ]
        borders = {'bottom': Line(stroke_color=navy)}
        
    class band_page_footer(ReportBand):
        height = 0.5*cm
        elements = [Label(text='Created with Geraldo Reports', top=0.1*cm, right=0), SystemField(expression='Printed in %(now:%Y, %b %d)s at %(now:%H:%M)s', top=0.1*cm, width=BAND_WIDTH, style={'alignment': TA_RIGHT}),]
        borders = {'top': True}

    class band_detail(ReportBand):
        height = 0.7*cm
        elements = [
             ObjectValue(attribute_name='id', top=0, left=0),
             ObjectValue(attribute_name='username', top=0, left=3*cm, display_format='<font size=14 name=Helvetica>%s</font>'),
             ObjectValue(attribute_name='first_name', top=0, left=8*cm),
             ObjectValue(attribute_name='last_name', top=0, left=13*cm),
             ObjectValue(attribute_name='is_staff', top=0, left=18*cm,
                 get_value=lambda instance: instance.is_staff and 'Yes' or 'No'),
        ]

        