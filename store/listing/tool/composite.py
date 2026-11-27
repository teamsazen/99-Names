"""Map real app screenshots onto chroma-green phone screens (store-screenshots/public/assets).
usage: screen.py out.png asset.png shot1.png [shot2.png]   (shot1 -> front/left-most screen)"""
import sys, numpy as np, cv2
SCALE = 2

def corners_of(comp):
    cnts,_ = cv2.findContours(comp, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)
    c = max(cnts, key=cv2.contourArea); hull = cv2.convexHull(c)
    h = hull.reshape(-1,2).astype(float); cx,cy = h.mean(0)
    # initial corners: farthest hull points in each diagonal direction
    ap = np.array([h[np.argmin(h[:,0]+h[:,1])], h[np.argmin(-h[:,0]+h[:,1])],
                   h[np.argmax(h[:,0]+h[:,1])], h[np.argmax(-h[:,0]+h[:,1])]])
    # refine: fit a line to each side's middle 70% and intersect
    pts = c.reshape(-1,2).astype(float)
    lines=[]
    for i in range(4):
        a,b = ap[i], ap[(i+1)%4]; d=b-a; L=np.linalg.norm(d); u=d/L; n=np.array([-u[1],u[0]])
        rel = pts-a; t = rel@u; dist = np.abs(rel@n)
        sel = pts[(t>0.2*L)&(t<0.8*L)&(dist<0.04*L)]
        if len(sel)<10: sel=np.array([a,b])
        vx,vy,x0,y0 = cv2.fitLine(sel.astype(np.float32), cv2.DIST_L2,0,0.01,0.01).ravel()
        lines.append((np.array([x0,y0]),np.array([vx,vy])))
    out=[]
    for i in range(4):
        (p1,d1),(p2,d2) = lines[i-1], lines[i]
        A=np.array([d1,-d2]).T; s=np.linalg.solve(A,p2-p1); out.append(p1+s[0]*d1)
    q=np.array(out)
    ys=q[np.argsort(q[:,1])]; top=ys[:2][np.argsort(ys[:2,0])]; bot=ys[2:][np.argsort(ys[2:,0])]
    return np.array([top[0],top[1],bot[1],bot[0]])  # TL TR BR BL

def main():
    out, asset, shots = sys.argv[1], sys.argv[2], sys.argv[3:]
    im = cv2.imread(asset, cv2.IMREAD_UNCHANGED)
    if im.shape[2]==3: im=cv2.cvtColor(im,cv2.COLOR_BGR2BGRA)
    im = cv2.resize(im,None,fx=SCALE,fy=SCALE,interpolation=cv2.INTER_LANCZOS4)
    f = im[:,:,:3].astype(np.float32); B,G,R = f[...,0],f[...,1],f[...,2]
    gness = G-np.maximum(R,B)
    core = ((gness>110)&(G>170)).astype(np.uint8)
    n,lab,st,_ = cv2.connectedComponentsWithStats(core)
    comps = sorted(range(1,n), key=lambda i:-st[i,cv2.CC_STAT_AREA])[:len(shots)]
    quads=[]; masks=[]; cores=[]
    for i in comps:
        m=(lab==i).astype(np.uint8)*255
        # fill the frame's punch-hole camera: the screen is shown whole, with no hole
        cnts,_=cv2.findContours(m,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
        m=np.zeros_like(m); cv2.drawContours(m,cnts,-1,255,-1)
        masks.append(cv2.dilate(m,np.ones((13,13),np.uint8))>0); cores.append(m>0)
        m=cv2.morphologyEx(m,cv2.MORPH_CLOSE,np.ones((25,25),np.uint8))
        quads.append(corners_of(m))
    # order: largest/front first already. If 2 screens, the smaller-visible one is the back phone:
    if len(quads)==2:
        front, back = quads
        k = np.linalg.norm(back[2]-back[1])/np.linalg.norm(front[2]-front[1])
        # back's right edge is true; rebuild its left edge from the front screen's shape
        back = np.array([back[1]+(front[0]-front[1])*k, back[1], back[2], back[2]+(front[3]-front[2])*k])
        quads=[front,back]
    alpha = np.clip((gness-25)/(150-25),0,1)
    # inside each screen the photo's green texture must not show through: fully opaque away from the edge
    for m in cores: alpha[cv2.erode(m.astype(np.uint8),np.ones((9,9),np.uint8))>0]=1
    res = f.copy()
    # despill partially-green edge pixels
    spill = alpha>0
    res[...,1][spill] = np.minimum(G, np.maximum(R,B))[spill]
    acc = np.zeros_like(f); wsum = np.zeros(f.shape[:2],np.float32); taken=np.zeros(f.shape[:2],bool); scrs=[]
    for q,sp in zip(quads,shots):
        s = cv2.imread(sp, cv2.IMREAD_COLOR).astype(np.float32)
        h,w = s.shape[:2]
        src=np.array([[0,0],[w,0],[w,h],[0,h]],np.float32)
        # expand quad 1.5% so rounded corners/antialias are fully covered
        c=q.mean(0); dst=((q-c)*1.015+c).astype(np.float32)
        H=cv2.getPerspectiveTransform(src,dst)
        warped=cv2.warpPerspective(s,H,(f.shape[1],f.shape[0]),flags=cv2.INTER_CUBIC,borderMode=cv2.BORDER_REPLICATE)
        reg=(masks[len(scrs)] & ~taken).astype(np.float32); taken|=masks[len(scrs)]; scrs.append(1)
        acc+=warped*reg[...,None]; wsum+=reg
        print(sp, "quad", np.round(q/SCALE,1).tolist())
    wsum=np.maximum(wsum,1e-6); scr=acc/wsum[...,None]
    a=alpha[...,None]
    res = res*(1-a)+scr*a
    outim = np.dstack([np.clip(res,0,255).astype(np.uint8), im[:,:,3]])
    cv2.imwrite(out,outim)
main()
