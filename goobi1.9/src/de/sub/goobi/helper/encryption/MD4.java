package de.sub.goobi.helper.encryption;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */public class MD4 {

   private static int A, B, C, D;
   private static int X[] = new int[16];

   private static int F(int X, int Y, int Z) {
      return (X & Y) | ((~X) & Z);
   }

   private static int G(int X, int Y, int Z) {
      return (X & Y) | (X & Z) | (Y & Z);
   }

   private static int H(int X, int Y, int Z) {
      return X ^ Y ^ Z;
   }

   private static int lshift(int x, int s) {
      if (s == 0) {
		return x;
	}
      return (((x << s) & 0xFFFFFFFF) | ((x >> (32 - s)) & (0x7FFFFFFF >> (31 - s))));
   }

   private static int ROUND1(int a, int b, int c, int d, int k, int s) {
      return (lshift(a + F(b, c, d) + X[k], s));
   }

   private static int ROUND2(int a, int b, int c, int d, int k, int s) {
      return (lshift(a + G(b, c, d) + X[k] + 0x5A827999, s));
   }

   private static int ROUND3(int a, int b, int c, int d, int k, int s) {
      return (lshift(a + H(b, c, d) + X[k] + 0x6ED9EBA1, s));
   }

   /* this applies md4 to 64 byte chunks */
   public static void mdfour64(int M[]) {
      int j;
      int AA, BB, CC, DD;

      for (j = 0; j < 16; j++) {
		X[j] = M[j];
	}

      AA = A;
      BB = B;
      CC = C;
      DD = D;

      A = ROUND1(A, B, C, D, 0, 3);
      D = ROUND1(D, A, B, C, 1, 7);
      C = ROUND1(C, D, A, B, 2, 11);
      B = ROUND1(B, C, D, A, 3, 19);
      A = ROUND1(A, B, C, D, 4, 3);
      D = ROUND1(D, A, B, C, 5, 7);
      C = ROUND1(C, D, A, B, 6, 11);
      B = ROUND1(B, C, D, A, 7, 19);
      A = ROUND1(A, B, C, D, 8, 3);
      D = ROUND1(D, A, B, C, 9, 7);
      C = ROUND1(C, D, A, B, 10, 11);
      B = ROUND1(B, C, D, A, 11, 19);
      A = ROUND1(A, B, C, D, 12, 3);
      D = ROUND1(D, A, B, C, 13, 7);
      C = ROUND1(C, D, A, B, 14, 11);
      B = ROUND1(B, C, D, A, 15, 19);

      A = ROUND2(A, B, C, D, 0, 3);
      D = ROUND2(D, A, B, C, 4, 5);
      C = ROUND2(C, D, A, B, 8, 9);
      B = ROUND2(B, C, D, A, 12, 13);
      A = ROUND2(A, B, C, D, 1, 3);
      D = ROUND2(D, A, B, C, 5, 5);
      C = ROUND2(C, D, A, B, 9, 9);
      B = ROUND2(B, C, D, A, 13, 13);
      A = ROUND2(A, B, C, D, 2, 3);
      D = ROUND2(D, A, B, C, 6, 5);
      C = ROUND2(C, D, A, B, 10, 9);
      B = ROUND2(B, C, D, A, 14, 13);
      A = ROUND2(A, B, C, D, 3, 3);
      D = ROUND2(D, A, B, C, 7, 5);
      C = ROUND2(C, D, A, B, 11, 9);
      B = ROUND2(B, C, D, A, 15, 13);

      A = ROUND3(A, B, C, D, 0, 3);
      D = ROUND3(D, A, B, C, 8, 9);
      C = ROUND3(C, D, A, B, 4, 11);
      B = ROUND3(B, C, D, A, 12, 15);
      A = ROUND3(A, B, C, D, 2, 3);
      D = ROUND3(D, A, B, C, 10, 9);
      C = ROUND3(C, D, A, B, 6, 11);
      B = ROUND3(B, C, D, A, 14, 15);
      A = ROUND3(A, B, C, D, 1, 3);
      D = ROUND3(D, A, B, C, 9, 9);
      C = ROUND3(C, D, A, B, 5, 11);
      B = ROUND3(B, C, D, A, 13, 15);
      A = ROUND3(A, B, C, D, 3, 3);
      D = ROUND3(D, A, B, C, 11, 9);
      C = ROUND3(C, D, A, B, 7, 11);
      B = ROUND3(B, C, D, A, 15, 15);

      A += AA;
      B += BB;
      C += CC;
      D += DD;

      A &= 0xFFFFFFFF;
      B &= 0xFFFFFFFF;
      C &= 0xFFFFFFFF;
      D &= 0xFFFFFFFF;
   }

   public static void copy64(int M[], byte in[], int offset) {
      int i;

      for (i = 0; i < 16; i++) {
		M[i] = ((in[offset + i * 4 + 3] << 24) & 0xFF000000) | ((in[offset + i * 4 + 2] << 16) & 0xFF0000)
               | ((in[offset + i * 4 + 1] << 8) & 0xFF00) | (in[offset + i * 4 + 0] & 0xFF);
	}
   }

   public static void copy64(int M[], byte in[]) {
      copy64(M, in, 0);
   }

   public static void copy4(byte out[], int offset, int x) {
      out[offset] = (byte) (x & 0xFF);
      out[1 + offset] = (byte) ((x >> 8) & 0xFF);
      out[2 + offset] = (byte) ((x >> 16) & 0xFF);
      out[3 + offset] = (byte) ((x >> 24) & 0xFF);
   }

   /* produce a md4 message digest from data of length n bytes */
   public static byte[] mdfour(byte in[]) {
      byte out[] = new byte[16];
      byte buf[] = new byte[128];
      int n = in.length;
      int M[] = new int[16];
      int b = n * 8;
      int i;
      int offset;

      A = 0x67452301;
      B = 0xefcdab89;
      C = 0x98badcfe;
      D = 0x10325476;

      offset = 0;
      while (n > 64) {
         copy64(M, in, offset);
         mdfour64(M);
         offset += 64;
         n -= 64;
      }

      for (i = 0; i < 128; i++) {
		buf[i] = (i + offset < in.length) ? in[offset + i] : 0;
	}
      buf[n] = (byte) 0x80;

      if (n <= 55) {
         copy4(buf, 56, b);
         copy64(M, buf);
         mdfour64(M);
      } else {
         copy4(buf, 120, b);
         copy64(M, buf);
         mdfour64(M);
         copy64(M, buf, 64);
         mdfour64(M);
      }

      for (i = 0; i < 128; i++) {
		buf[i] = 0;
	}
      copy64(M, buf);

      copy4(out, 0, A);
      copy4(out, 4, B);
      copy4(out, 8, C);
      copy4(out, 12, D);

      A = B = C = D = 0;
      return out;
   }

}
