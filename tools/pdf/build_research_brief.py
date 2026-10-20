# Builds docs/client/NUHA_Research_Brief.pdf from docs/research/research-brief.md.
# Reuses the setup (fonts, styles, Arabic handling) of build_legacy_reference.py.
from pathlib import Path
base=Path('tools/pdf/build_legacy_reference.py').read_text()
setup=base.split('story=[]')[0]
exec(setup)
styles['BodyText'].fontSize=10.5
styles['BodyText'].leading=15.5
styles['Bullet2'].fontSize=10.5
styles['Bullet2'].leading=15.5
styles['Cell'].fontSize=9
styles['Cell'].leading=13
text=Path('docs/research/research-brief.md').read_text()
refs=dict(re.findall(r'\[(S\d+)\][^\n]+?(https://\S+)',text))
oldfmt=fmt
def fmt(s):
 urls=re.findall(r'https://\S+',s)
 for j,u in enumerate(urls):s=s.replace(u,f'URLTOKEN{j}')
 s=oldfmt(s)
 for j,u in enumerate(urls):s=s.replace(f'URLTOKEN{j}',f'<link href="{html.escape(u,quote=True)}" color="#137D87"><u>Open source</u></link>')
 s=re.sub(r'\[(S\d+)\]',lambda m:f'<link href="{html.escape(refs.get(m.group(1),""),quote=True)}" color="#137D87">[{m.group(1)}]</link>',s)
 return s
story=[]
def para(t,style='BodyText'):return Paragraph(fmt(t),styles[style])
story += [Spacer(1,75),para('NUHA','Heading1'),para('Research for the New Educational Experience','Heading1'),Spacer(1,15),para('Children • Arabic learning • Accessible play • Kotlin','Heading3'),para('Client research brief | 20 October 2026'),Spacer(1,25),para('From legacy observations to evidence-informed decisions','Heading2'),para('A practical synthesis for the smart educational table and its next Android app. Covers the nine intended activities, the needs of children and adults, measurement, hardware, privacy, and implementation experiments.'),Spacer(1,15),para('How to read this report','Heading3'),para('Findings cite their sources. Recommendations are proposals to test. Field questions identify what desk research cannot settle.'),para('No child interviews or user tests were conducted for this report. No medical benefit or legal compliance is asserted.'),Spacer(1,18),para('20 external sources • 9 game briefs • A field-research roadmap','Heading3')]
lines=text.splitlines()[1:];i=0
while i<len(lines):
 line=lines[i].strip()
 if not line:i+=1;continue
 if line.startswith('## '):story += [PageBreak(),para(line[3:],'Heading2')]
 elif line.startswith('### '):story.append(para(line[4:],'Heading3'))
 elif line.startswith('|'):
  rows=[]
  while i<len(lines) and lines[i].strip().startswith('|'):
   cells=[c.strip() for c in lines[i].strip().strip('|').split('|')]
   if not all(re.fullmatch(r':?-+:?',c) for c in cells):rows.append([para(c,'Cell') for c in cells])
   i+=1
  widths=[487/len(rows[0])]*len(rows[0])
  if len(rows[0])==3:widths=[115,205,167]
  t=Table(rows,colWidths=widths,repeatRows=1,hAlign='LEFT')
  t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#DFEBEF')),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#F5F8FA')]),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7)]))
  story.extend([t,Spacer(1,10)]);continue
 elif line.startswith('- '):story.append(para('• '+line[2:],'Bullet2'))
 else:story.append(para(line))
 i+=1
out='docs/client/NUHA_Research_Brief.pdf'
def footer(c,d):
 c.setStrokeColor(colors.HexColor('#D6E1E7'));c.line(54,43,541,43)
 c.setFont('Body',8);c.setFillColor(navy);c.drawString(54,30,'NUHA | Research brief | 20 October 2026');c.drawRightString(541,30,str(d.page))
 if d.page>1:c.drawString(54,810,'RESEARCH FINDINGS / DESIGN RECOMMENDATIONS / OPEN QUESTIONS')
doc=SimpleDocTemplate(out,pagesize=(595.28,841.89),rightMargin=54,leftMargin=54,topMargin=53,bottomMargin=59,title='NUHA - Research for the New Educational Experience',author='NUHA')
doc.build(story,onFirstPage=footer,onLaterPages=footer)
import pdfplumber
Path('tmp/pdf-previews/research').mkdir(parents=True,exist_ok=True)
from PIL import Image
with pdfplumber.open(out) as pdf:
 print('Pages:',len(pdf.pages),'Sources:',len(refs))
 thumbs=[]
 for i,p in enumerate(pdf.pages):
  im=p.to_image(resolution=95).original.convert('RGB');im.save(f'tmp/pdf-previews/research/page-{i+1}.png')
  im.thumbnail((298,421));thumbs.append(im)
  chars=[c for c in p.chars if c.get('text','').strip() and (c['x0']<45 or c['x1']>551)]
  if chars: print('CHECK bounds page',i+1, len(chars))
 for start in range(0,len(thumbs),6):
  sheet=Image.new('RGB',(894,882),'#bdc6ce')
  for j,im in enumerate(thumbs[start:start+6]):sheet.paste(im,((j%3)*298,(j//3)*441))
  sheet.save(f'tmp/pdf-previews/research/contact-{start//6+1}.png')
 print('PDF external links:',sum(len(p.hyperlinks) for p in pdf.pages))
