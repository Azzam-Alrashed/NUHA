# Builds docs/client/NUHA_Legacy_App_Reference.pdf from docs/legacy-app-reference.md.
# Run from the repo root with a Python that has reportlab, pdfplumber and Pillow,
# plus arabic-reshaper and python-bidi installed in /tmp/nuha-pdf-deps (see sys.path below).
import sys,re,html
sys.path.insert(0,'/tmp/nuha-pdf-deps')
import arabic_reshaper
from bidi.algorithm import get_display
from pathlib import Path
from reportlab.platypus import SimpleDocTemplate,Paragraph,Spacer,Table,TableStyle,PageBreak,Preformatted
from reportlab.lib.styles import getSampleStyleSheet,ParagraphStyle
from reportlab.lib import colors
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_LEFT
for n,f in [('Body','Arial.ttf'),('Bold','Arial Bold.ttf'),('Arabic','Arial Unicode.ttf')]:
 pdfmetrics.registerFont(TTFont(n,'/System/Library/Fonts/Supplemental/'+f))
pdfmetrics.registerFontFamily('Body',normal='Body',bold='Bold',italic='Body',boldItalic='Bold')
navy=colors.HexColor('#183047'); teal=colors.HexColor('#137D87')
styles=getSampleStyleSheet()
for n in ['BodyText','Normal']:
 styles[n].fontName='Body';styles[n].fontSize=9.4;styles[n].leading=14;styles[n].spaceAfter=7
for n,size in [('Heading1',23),('Heading2',16),('Heading3',11.5)]:
 styles[n].fontName='Bold';styles[n].fontSize=size;styles[n].leading=size*1.25;styles[n].textColor=navy;styles[n].spaceBefore=14;styles[n].spaceAfter=8;styles[n].keepWithNext=True
styles.add(ParagraphStyle(name='Cell',fontName='Body',fontSize=8.3,leading=12,spaceAfter=0))
styles.add(ParagraphStyle(name='Bullet2',parent=styles['BodyText'],leftIndent=12,firstLineIndent=-9))
def fmt(s):
 s=re.sub(r'\[([^]]+)\]\(<[^>]+>\)',r'\1',s)
 s=s.replace('–','-').replace('—','-').replace('‑','-')
 s=html.escape(s)
 s=re.sub(r'([\u0600-\u06ff]+(?:[\s،؛؟]*[\u0600-\u06ff]+)*)',lambda m:'<font name="Arabic">'+get_display(arabic_reshaper.reshape(m.group()))+'</font>',s)
 s=re.sub(r'\*\*(.*?)\*\*',r'<b>\1</b>',s)
 s=re.sub(r'`([^`]+)`',r'<font color="#137D87">\1</font>',s)
 return s
story=[]
def para(t,style='BodyText'):return Paragraph(fmt(t),styles[style])
story += [Spacer(1,90),para('NUHA','Heading1'),para('Legacy Android App Reference','Heading1'),para('لوحتي الذكية','Heading2'),Spacer(1,20),para('Client reference | 20 October 2026'),para('Version 2.4.0 • Product walkthrough and evidence record'),Spacer(1,25),para('Purpose','Heading2'),para('A record of the legacy application to inform the new Kotlin version and its UI/UX design. Observed behavior is separated from advertised capabilities, original design intentions, and unresolved questions.'),para('This document is not a specification for the replacement and does not establish medical effectiveness.'),Spacer(1,25),para('Reading guide','Heading2'),para('Context and environment • Parent and child workflows • Ten games and focus challenge • Results and accessibility • Installation • Design differences and open questions'),PageBreak()]
lines=Path('docs/legacy-app-reference.md').read_text().splitlines()[1:];i=0
while i<len(lines):
 line=lines[i].strip()
 if not line:i+=1;continue
 if line.startswith('```'):
  block=[];i+=1
  while i<len(lines) and not lines[i].startswith('```'):block.append(lines[i]);i+=1
  for b in block:story.append(para(b,'Cell'))
  story.append(Spacer(1,10));i+=1;continue
 if line.startswith('|'):
  rows=[]
  while i<len(lines) and lines[i].strip().startswith('|'):
   cells=[c.strip() for c in lines[i].strip().strip('|').split('|')]
   if not all(re.fullmatch(r':?-+:?',c) for c in cells):rows.append([para(c,'Cell') for c in cells])
   i+=1
  widths=[487/len(rows[0])]*len(rows[0])
  t=Table(rows,colWidths=widths,repeatRows=1,hAlign='LEFT')
  t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#DFEBEF')),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#F5F8FA')]),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7),('LINEBELOW',(0,0),(-1,0),.7,teal)]));story.extend([t,Spacer(1,10)]);continue
 if line.startswith('### '):story.append(para(line[4:],'Heading3'))
 elif line.startswith('## '):story.append(para(line[3:],'Heading2'))
 elif line.startswith('- '):story.append(para('• '+line[2:],'Bullet2'))
 else:story.append(para(line))
 i+=1
def footer(c,d):
 c.setStrokeColor(colors.HexColor('#D6E1E7'));c.line(54,43,541,43)
 c.setFont('Body',8);c.setFillColor(navy);c.drawString(54,30,'NUHA | Legacy app reference');c.drawRightString(541,30,str(d.page))
 if d.page>1:c.drawString(54,810,'LOUHATI AL-DHAKIYA  /  LEGACY VERSION 2.4.0')
doc=SimpleDocTemplate('docs/client/NUHA_Legacy_App_Reference.pdf',pagesize=(595.28,841.89),rightMargin=54,leftMargin=54,topMargin=53,bottomMargin=59,title='NUHA - Legacy App Reference',author='NUHA',allowSplitting=1)
doc.build(story,onFirstPage=footer,onLaterPages=footer)
import pdfplumber
Path('tmp/pdf-previews/legacy').mkdir(parents=True,exist_ok=True)
with pdfplumber.open('docs/client/NUHA_Legacy_App_Reference.pdf') as pdf:
 print('Pages:',len(pdf.pages))
 from PIL import Image,ImageOps,ImageDraw
 thumbs=[]
 for i,p in enumerate(pdf.pages):
  im=p.to_image(resolution=85).original.convert('RGB');im.save(f'tmp/pdf-previews/legacy/page-{i+1}.png')
  im.thumbnail((298,421));thumbs.append(im)
 for start in range(0,len(thumbs),6):
  sheet=Image.new('RGB',(894,882),'#bcc5cc')
  for j,im in enumerate(thumbs[start:start+6]):sheet.paste(im,((j%3)*298,(j//3)*441))
  sheet.save(f'tmp/pdf-previews/legacy/contact-{start//6+1}.png')
