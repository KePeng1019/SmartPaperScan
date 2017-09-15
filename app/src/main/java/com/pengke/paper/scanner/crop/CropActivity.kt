package com.pengke.paper.scanner.crop

import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import com.pengke.paper.scanner.R
import com.pengke.paper.scanner.base.BaseActivity
import com.pengke.paper.scanner.view.PaperRectangle
import kotlinx.android.synthetic.main.activity_crop.*

class CropActivity : BaseActivity(), ICropView.Proxy {

    private var mPresenter: CropPresenter? = null

    override fun prepare() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.crop_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.crop -> {
                mPresenter?.crop()
            }
            R.id.enhance -> {
                mPresenter?.enhance()
            }
            R.id.reset -> {
                mPresenter?.reset()
            }
            R.id.back -> {
                mPresenter?.black()
            }
            R.id.save -> {
                mPresenter?.save()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun provideContentViewId(): Int = R.layout.activity_crop


    override fun initPresenter() {
        mPresenter = CropPresenter(this, this)
    }

    override fun getPaper(): ImageView = paper

    override fun getPaperRect(): PaperRectangle = paper_rect
}