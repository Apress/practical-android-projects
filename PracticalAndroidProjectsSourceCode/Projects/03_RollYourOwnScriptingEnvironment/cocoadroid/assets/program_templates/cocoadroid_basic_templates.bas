rem -- CocoaDroid BASIC
rem -- Sample Programs
rem -- 2010.11.25
rem -- http://pietergreyling.com

rem -- SCRIPT 01 --
data 11, 22, 33
read x, y, z
print x, y, z
rx = log(x)
ry = sin(y)
rz = sqr(z)
print "log(x) = " + str$(rx)
print "sin(y) = " + str$(ry)
print "sqr(z) = " + str$(rz)

rem -- SCRIPT 02 --
print "Six lucky dice throws!"
c = 6 : dim a(c)
for i = 1 to c: randomize: a(i) = int(rnd(6)) + 1: next i
for i = 1 to c: print a(i): next i
print "Want to try again?"

rem -- PROGRAM 01 --
100 for i=1 to 10
110     gosub 500
120     gosub 600
130 next i
140 end
500 rem -- a sub-routine
510 if i < 5 then print "<5"
520 if i > 4 then print "4>"
530 return
600 rem -- another sub
610 if i < 3 then print "<3"
620 if i > 2 then print "2>"
630 return
900 end
list
run

rem -- PROGRAM 02 --
000 print "-- running..."
005 data "aaa", "bb", "c"
010 dim a$(3)
020 read a$(1), a$(2), a$(3)
030 for i = 1 to 3
040     print a$(i)
050 next i
900 end
list
run

rem -- PROGRAM 03 dice 2 --
010 print ">> Six lucky dice!"
020 let cnt = 6 : dim a(cnt)
030 for i = 1 to cnt
040     randomize
050     a(i) = int(rnd(6))+1
060 next i
070 for i = 1 to cnt
080     print a(i)
090 next i
100 print ">> Try again?"
900 end
list
run

rem -- PROGRAM 04 --
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
220 end
list
run

rem -- PROGRAM 05 --
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
210 print "-- end RANDOM"
220 gosub 333
230 print ">> end STRINGS"
240 gosub 433
250 print ">> end NUMBERS"
255 rem -- end of execution
260 end
322 rem -- string data
333 read skey$, s1$, s2$, s3$
344 print skey$, s1$, s2$, s3$
355 return
422 rem -- numeric data
433 read skey$, n1, n2, n3
444 print skey$, n1, n2, n3
455 return
500 rem -- data storage
510 data "SDATA", "U", "D", "T"
520 data "NDATA", 11, 22, 33
list
run
