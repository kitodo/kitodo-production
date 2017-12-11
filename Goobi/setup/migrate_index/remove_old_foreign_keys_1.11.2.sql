--
-- Remove old FOREIGN KEY of Goobi.Production CE 1.11.2
--
--
ALTER TABLE `batchesprozesse`
  DROP FOREIGN KEY `FK_73ljen2jowf97kh4v4p1hlo8r`,
  DROP FOREIGN KEY `FK_5dqpl4devujxpajjjsvosl9l2`;

ALTER TABLE `benutzer`
  DROP FOREIGN KEY `FK_ialwcbckq8uhe1g3o7kdxwxaw`;

ALTER TABLE `benutzereigenschaften`
  DROP FOREIGN KEY `FK_h33lql0vqubolfh9int4nlsrg`;

ALTER TABLE `benutzergruppenmitgliedschaft`
  DROP FOREIGN KEY `FK_itxitbdb67yhc61wxfvmyrjqj`,
  DROP FOREIGN KEY `FK_b8y7wu06hml1vht3rovasacfn`;

ALTER TABLE `history`
  DROP FOREIGN KEY `FK_fqac1yfoo5xme3q991k60yl2o`;

ALTER TABLE `projectfilegroups`
  DROP FOREIGN KEY `FK_rpcxamphryu9dhhsi9ir8m8bh`;

ALTER TABLE `projektbenutzer`
  DROP FOREIGN KEY `FK_9a3stxxcpuyk11wyjrq3qptdp`,
  DROP FOREIGN KEY `FK_snqs6v0oejd07ifph1yf16dt7`;

ALTER TABLE `prozesse`
  DROP FOREIGN KEY `FK_1y0bi6cei5fbkcuoaodc2nq17`,
  DROP FOREIGN KEY `FK_4roxax8915n8m6yuaci4hxw6d`,
  DROP FOREIGN KEY `FK_eh0me4v96c13haiati7xlx3hf`;

ALTER TABLE `prozesseeigenschaften`
  DROP FOREIGN KEY `FK_opu3up0chr73x5i1mi586d7gi`;

ALTER TABLE `schritte`
  DROP FOREIGN KEY `FK_aj3rxm2sd4qcega1v8k7lr7aj`,
  DROP FOREIGN KEY `FK_hx9btwrl6bp5q3iwmiyh24oj8`;

ALTER TABLE `schritteberechtigtebenutzer`
  DROP FOREIGN KEY `FK_t3wjnvlpbwyrca8qwb1woetw7`,
  DROP FOREIGN KEY `FK_2p6vm4hscq2ngy6j7lh26tbun`;

ALTER TABLE `schritteberechtigtegruppen`
  DROP FOREIGN KEY `FK_2i17p29fvaorqkfw0b6xam1bt`,
  DROP FOREIGN KEY `FK_9ygjhlmy2cn7yk6c4dhpyb5qa`;

ALTER TABLE `vorlagen`
  DROP FOREIGN KEY `FK_arv9kxhj0tkjf68s17gyenbn7`;

ALTER TABLE `vorlageneigenschaften`
  DROP FOREIGN KEY `FK_6c95356eoc02jdpx29cqv49py`;

ALTER TABLE `werkstuecke`
  DROP FOREIGN KEY `FK_8gxqj75v93434jew1spke8cj9`;

ALTER TABLE `werkstueckeeigenschaften`
  DROP FOREIGN KEY `FK_ig1c1vxjmm796nc31sgvijo3s`;
