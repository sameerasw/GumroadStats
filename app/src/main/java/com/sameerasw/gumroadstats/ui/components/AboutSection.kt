package com.sameerasw.gumroadstats.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.sameerasw.gumroadstats.R

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
fun AboutSection(
    modifier: Modifier = Modifier,
    appName: String = stringResource(R.string.app_name),
    developerName: String = "Sameera Wijerathna",
    description: String = stringResource(R.string.app_description),
    onAvatarLongClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (_: Exception) {
        "Unknown"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceBright
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "$appName v$versionName", style = MaterialTheme.typography.headlineLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Image(
                painter = painterResource(id = R.drawable.avatar),
                contentDescription = "Developer Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            onAvatarLongClick()
                        }
                    )
            )

            Text(
                text = stringResource(R.string.developed_by_sameera),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 3
            ) {
                Button(
                    onClick = {
                        val websiteUrl = "https://sameerasw.com"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_web_traffic_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.website))
                }

                Button(
                    onClick = {
                        val websiteUrl = "https://github.com/sameerasw/GumroadStats"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.brand_github),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.github))
                }

                OutlinedButton(
                    onClick = {
                        val websiteUrl = "https://t.me/tidwib"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.brand_telegram),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.telegram))
                }

                OutlinedButton(
                    onClick = {
                        val websiteUrl = "https://buymeacoffee.com/sameerasw"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_heart_smile_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.support))
                }
            }

            Text(
                text = stringResource(R.string.other_apps),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                maxItemsInEachRow = 3
            ) {

                OutlinedButton(
                    onClick = {
                        val websiteUrl =
                            "https://play.google.com/store/apps/details?id=com.sameerasw.airsync"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_devices_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AirSync")
                }

                OutlinedButton(
                    onClick = {
                        val websiteUrl = "https://github.com/sameerasw/essentials"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.essentials_icon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.essentials))
                }

                OutlinedButton(
                    onClick = {
                        val websiteUrl = "https://sameerasw.com/zen"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_web_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.zenzero))
                }

                OutlinedButton(
                    onClick = {
                        val websiteUrl = "https://github.com/sameerasw/canvas"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_draw_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.canvas))
                }

                OutlinedButton(
                    onClick = {
                        val websiteUrl = "https://github.com/sameerasw/tasks"
                        val intent = Intent(Intent.ACTION_VIEW, websiteUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_task_alt_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.tasks))
                }
            }
        }
    }
}
