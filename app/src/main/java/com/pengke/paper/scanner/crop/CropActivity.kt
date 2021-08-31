package com.pengke.paper.scanner.crop

import android.view.View
import android.widget.ImageView
import com.pengke.paper.scanner.R
import com.pengke.paper.scanner.base.BaseActivity
import com.pengke.paper.scanner.view.PaperRectangle
import kotlinx.android.synthetic.main.activity_crop.*

class CropActivity : BaseActivity(), ICropView.Proxy {

    private lateinit var mPresenter: CropPresenter

    override fun prepare() {
        crop.setOnClickListener {
            crop.visibility = View.INVISIBLE
            enhance.visibility = View.VISIBLE
            mPresenter.crop()
        }
        enhance.setOnClickListener {
            crop.visibility = View.INVISIBLE
            enhance.visibility = View.INVISIBLE
            mPresenter.enhance()
        }
        proceed.setOnClickListener { mPresenter.proceed() }
    }

    override fun provideContentViewId(): Int = R.layout.activity_crop


    override fun initPresenter() {
        mPresenter = CropPresenter(this, this)
    }

    override fun getPaper(): ImageView = paper

    override fun getPaperRect(): PaperRectangle = paper_rect

    override fun getCroppedPaper(): ImageView = picture_cropped
}