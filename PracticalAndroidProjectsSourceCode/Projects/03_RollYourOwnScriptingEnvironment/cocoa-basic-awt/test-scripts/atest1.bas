010 rem -- 
020 rem -- file: atest.bas
030 rem -- desc: test cocoa-cocoa.basic
040 rem -- date: 2010.11.25
050 rem -- auth: pieter greyling (http://pietergreyling.com)
060 rem -- 
100 r05 = rnd(05)
110 r10 = rnd(10)
120 r15 = rnd(15)
130 r20 = rnd(20)
140 r = r05+r10+r15+r20
150 print "r05: " + str$(r05)
160 print "r10: " + str$(r10)
170 print "r15: " + str$(r15)
180 print "r20: " + str$(r20)
190 print "-----"
200 print "r  : " + str$(r)
210 print "done"
220 rem -- ends: atest.bas