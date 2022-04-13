package com.mediatek.wwtv.tvcenter.nav.util;

import com.mediatek.twoworlds.tv.MtkTvEWSPABase;

public final class MtkTvEWSPA extends MtkTvEWSPABase {

	private static MtkTvEWSPA instance;

	private MtkTvEWSPA() {

	}

	public static synchronized MtkTvEWSPA getInstance() {
		if (instance == null) {
			instance = new MtkTvEWSPA();
		}
		return instance;
	}
}
