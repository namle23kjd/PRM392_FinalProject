package com.example.prm392_finalproject.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm392_finalproject.R;
import com.example.prm392_finalproject.controllers.ReviewController;
import com.example.prm392_finalproject.dao.OrderDAO;
import com.example.prm392_finalproject.dao.ReviewDAO;
import com.example.prm392_finalproject.models.Product;
import com.example.prm392_finalproject.models.Review;
import com.example.prm392_finalproject.utils.CartManager;
import com.example.prm392_finalproject.views.adapters.ReviewAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CustomerProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "CustomerProductDetail";

    // UI Components - Product
    private ImageView ivProductImage;
    private TextView tvProductName, tvProductPrice, tvProductCategory;
    private TextView tvStockStatus, tvStockQuantity;
    private TextView tvProductDescription, tvProductColor, tvProductOrigin, tvProductWarranty;
    private Button btnAddToCart, btnBuyNow;

    // UI Components - Review
    private RatingBar ratingBarInput;
    private EditText edtComment;
    private Button btnSubmitReview;
    private RecyclerView recyclerViewReviews;
    private TextView tvAverageRating;

    // Others
    private Product product;
    private NumberFormat currencyFormat;
    private ReviewController reviewController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_product_detail);

        initViews();
        setupToolbar();
        getProductFromIntent();

        if (product != null) {
            displayProductData();
            loadReviews();
        }
    }

    private void initViews() {
        // Product Views
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductCategory = findViewById(R.id.tvProductCategory);
        tvStockStatus = findViewById(R.id.tvStockStatus);
        tvStockQuantity = findViewById(R.id.tvStockQuantity);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        tvProductColor = findViewById(R.id.tvProductColor);
        tvProductOrigin = findViewById(R.id.tvProductOrigin);
        tvProductWarranty = findViewById(R.id.tvProductWarranty);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);

        // Review Views (Thêm Mới)
        ratingBarInput = findViewById(R.id.ratingBarInput);
        edtComment = findViewById(R.id.edtComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        tvAverageRating = findViewById(R.id.tvAverageRating);

        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setNestedScrollingEnabled(false);

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnBuyNow.setOnClickListener(v -> buyNow());
        btnSubmitReview.setOnClickListener(v -> submitReview());

        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        reviewController = new ReviewController(new OrderDAO(), new ReviewDAO());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Product Details");
        }
    }

    private void getProductFromIntent() {
        try {
            product = (Product) getIntent().getSerializableExtra("product");
            if (product == null) {
                Log.e(TAG, "Product is null from intent");
                Toast.makeText(this, "Error: Product not found", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.d(TAG, "Product received: " + product.getName());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting product from intent: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayProductData() {
        tvProductName.setText(product.getName());
        tvProductPrice.setText(currencyFormat.format(product.getPrice()));
        tvProductCategory.setText(product.getCategory() != null ? product.getCategory() : "General");

        int stock = product.getQuantityInStock();
        if (stock > 0) {
            tvStockStatus.setText("In Stock");
            tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvStockQuantity.setText("(" + stock + " available)");
            btnAddToCart.setEnabled(true);
            btnBuyNow.setEnabled(true);
        } else {
            tvStockStatus.setText("Out of Stock");
            tvStockStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvStockQuantity.setText("(Not available)");
            btnAddToCart.setEnabled(false);
            btnBuyNow.setEnabled(false);
        }

        tvProductDescription.setText(product.getDescription() != null ? product.getDescription() : "No description available.");
        tvProductColor.setText(product.getColor() != null ? product.getColor() : "N/A");
        tvProductOrigin.setText(product.getOriginCountry() != null ? product.getOriginCountry() : "N/A");
        tvProductWarranty.setText(product.getWarrantyPeriod() != null ? product.getWarrantyPeriod() + " months" : "N/A");

        ivProductImage.setImageResource(R.drawable.ic_menu_business);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(product.getName());
        }
    }

    private void submitReview() {
        float rating = ratingBarInput.getRating();
        String comment = edtComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = new Review(0, 1, product.getProductId(), rating, comment);  // Giả định userId = 1
        reviewController.addReview(review);

        Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
        edtComment.setText("");
        ratingBarInput.setRating(0);

        loadReviews();
    }

    private void loadReviews() {
        List<Review> reviews = reviewController.getProductReviews(product.getProductId());

        ReviewAdapter adapter = new ReviewAdapter(this, reviews);
        recyclerViewReviews.setAdapter(adapter);

        float avg = reviewController.getAverageRating(product.getProductId());
        tvAverageRating.setText(String.format(Locale.US, "%.1f / 5", avg));
    }


    private void addToCart() {
        try {
            if (product != null) {
                CartManager.getInstance().addToCart(product);
                Snackbar.make(btnAddToCart, "Added to cart: " + product.getName(), Snackbar.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error adding to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNow() {
        try {
            if (product != null) {
                CartManager.getInstance().addToCart(product);
                Intent intent = new Intent(this, CartActivity.class);
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error processing purchase", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
