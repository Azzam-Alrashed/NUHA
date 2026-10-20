# Builds docs/client/NUHA_UI_UX_Style_Research.pdf from docs/research/ui-style.md,
# reusing the styling of the earlier client PDFs. Run from the repo root.
from pathlib import Path
base=Path('tools/pdf/build_legacy_reference.py').read_text()
setup=base.split('story=[]')[0]
# Keep Arabic phrases joined across ellipses so bidi reorders them as one run.
arabic_run=r"([\u0600-\u06ff]+(?:[\s،؛؟]*[\u0600-\u06ff]+)*)"
assert arabic_run in setup
setup=setup.replace(arabic_run,r"([\u0600-\u06ff]+(?:[\s،؛؟…]*[\u0600-\u06ff]+)*…?)")
exec(setup)
styles['BodyText'].fontSize=10
styles['BodyText'].leading=15
styles['Bullet2'].fontSize=10
styles['Bullet2'].leading=15
styles['Cell'].fontSize=8.6
styles['Cell'].leading=12.6
styles.add(ParagraphStyle(name='Lead',parent=styles['BodyText'],fontSize=11.5,leading=17.5,textColor=navy))
TEAL='#137D87'
oldfmt=fmt
def fmt(s):
 links=[]
 def keep(m):
  links.append((m.group(1),m.group(2)));return f'LINKTOKEN{len(links)-1}X'
 s=re.sub(r'\[([^\]]+)\]\((https?://[^)\s]+)\)',keep,s)
 s=re.sub(r'(?<![\*\w])\*([^*\n]+)\*(?![\*\w])',r'ITALSTART\1ITALEND',s)
 s=oldfmt(s)
 # A visually reordered Arabic run must not wrap mid-phrase, or its word order breaks across lines.
 s=re.sub(r'(<font name="Arabic">)(.*?)(</font>)',lambda m:m.group(1)+m.group(2).replace(' ','\u00a0')+m.group(3),s)
 s=s.replace('ITALSTART','<i>').replace('ITALEND','</i>')
 for j,(t,u) in enumerate(links):
  s=s.replace(f'LINKTOKEN{j}X',f'<link href="{html.escape(u,quote=True)}" color="{TEAL}"><u>{oldfmt(t)}</u></link>')
 return s
def para(t,style='BodyText'):return Paragraph(fmt(t),styles[style])
md=Path('docs/research/ui-style.md').read_text().splitlines()
headline=md[0].lstrip('# ').strip()
body=md[1:]
story=[Spacer(1,75),para('NUHA','Heading1'),para('UI and UX Style Research for the New Version','Heading1'),Spacer(1,12),
 para('Colour • Arabic type • Illustration • Interaction • Motion and sound • Rewards','Heading3'),
 para('Client research brief | 20 October 2026'),Spacer(1,25),
 para(headline,'Heading2'),
 para('A desk-research synthesis of what visual and interaction style the new Kotlin app should adopt for Arabic-speaking children with autism, ADHD, and learning difficulties, and how that style maps onto the existing Jetpack Compose code.'),
 Spacer(1,15),para('How to read this report','Heading3'),
 para('Each recommendation carries an evidence level: Strong, Moderate, Guideline, or Inference. Inferences are design judgements that must be tested with children on the real table.'),
 para('No child sessions or user tests were conducted for this report. No medical benefit is asserted.'),
 Spacer(1,18),para('5 research tracks • 14 style recommendations • 9 items to test with children','Heading3'),
 PageBreak(),para('Summary','Heading2')]
i=0
while i<len(body):
 line=body[i].strip()
 if not line:i+=1;continue
 if line.startswith('## '):
  story += [PageBreak(),para(line[3:],'Heading2')]
 elif line.startswith('### '):story.append(para(line[4:],'Heading3'))
 elif line.startswith('|'):
  rows=[]
  while i<len(body) and body[i].strip().startswith('|'):
   cells=[c.strip() for c in body[i].strip().strip('|').split('|')]
   if not all(re.fullmatch(r':?-+:?',c) for c in cells):rows.append([para(c,'Cell') for c in cells])
   i+=1
  widths={3:[92,258,137],2:[118,369]}.get(len(rows[0]),[487/len(rows[0])]*len(rows[0]))
  t=Table(rows,colWidths=widths,repeatRows=1,hAlign='LEFT')
  t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#DFEBEF')),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#F5F8FA')]),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),7),('RIGHTPADDING',(0,0),(-1,-1),7),('TOPPADDING',(0,0),(-1,-1),6),('BOTTOMPADDING',(0,0),(-1,-1),6),('LINEBELOW',(0,0),(-1,0),.7,teal)]))
  story.extend([t,Spacer(1,10)]);continue
 elif line.startswith('- '):story.append(para('• '+line[2:],'Bullet2'))
 else:story.append(para(line))
 i+=1
out='docs/client/NUHA_UI_UX_Style_Research.pdf'
def footer(c,d):
 c.setStrokeColor(colors.HexColor('#D6E1E7'));c.line(54,43,541,43)
 c.setFont('Body',8);c.setFillColor(navy);c.drawString(54,30,'NUHA | UI and UX style research | 20 October 2026');c.drawRightString(541,30,str(d.page))
 if d.page>1:c.drawString(54,810,'EVIDENCE / RECOMMENDED STYLE / TESTS WITH CHILDREN')
doc=SimpleDocTemplate(out,pagesize=(595.28,841.89),rightMargin=54,leftMargin=54,topMargin=53,bottomMargin=59,title='NUHA - UI and UX Style Research',author='NUHA')
doc.build(story,onFirstPage=footer,onLaterPages=footer)
import pdfplumber
Path('tmp/pdf-previews/ui-style').mkdir(parents=True,exist_ok=True)
from PIL import Image
with pdfplumber.open(out) as pdf:
 print('Pages:',len(pdf.pages))
 thumbs=[]
 for k,p in enumerate(pdf.pages):
  im=p.to_image(resolution=95).original.convert('RGB');im.save(f'tmp/pdf-previews/ui-style/page-{k+1}.png')
  im.thumbnail((298,421));thumbs.append(im)
  chars=[c for c in p.chars if c.get('text','').strip() and (c['x0']<45 or c['x1']>551)]
  if chars: print('CHECK bounds page',k+1,len(chars))
 for start in range(0,len(thumbs),6):
  sheet=Image.new('RGB',(894,882),'#bdc6ce')
  for j,im in enumerate(thumbs[start:start+6]):sheet.paste(im,((j%3)*298,(j//3)*441))
  sheet.save(f'tmp/pdf-previews/ui-style/contact-{start//6+1}.png')
 print('PDF external links:',sum(len(p.hyperlinks) for p in pdf.pages))
