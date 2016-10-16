010 rem -- 
020 rem -- file: atest2.bas
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
210 print "-- done with RND()"
220 gosub 333
230 print "-- done with STRING DATA READ"
240 gosub 433
250 print "-- done with NUMERIC DATA READ"
255 rem -- end of execution  - - - - - - - - - - - - - - - - - - - - - - - - -
260 end
322 rem -- string data reader routine  - - - - - - - - - - - - - - - - - - - -
333 read skey$, s1$, s2$, s3$
344 print skey$, s1$, s2$, s3$
355 return
422 rem -- numeric data reader routine - - - - - - - - - - - - - - - - - - - -
433 read skey$, n1, n2, n3
444 print skey$, n1, n2, n3
455 return
500 rem -- data storage  - - - - - - - - - - - - - - - - - - - - - - - - - - -
510 data "STRING_DATA", "one", "two", "three"
520 data "NUMBER_DATA", 111, 222, 333
999 rem -- ends: atest2.bas
